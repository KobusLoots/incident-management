package customer.incident_management.handler;

import cds.gen.api_business_partner.ABusinessPartner;
import cds.gen.api_business_partner.ABusinessPartner_;
import cds.gen.api_business_partner.ApiBusinessPartner;
import cds.gen.processorservice.Incidents;
import cds.gen.processorservice.Incidents_;
import cds.gen.processorservice.ProcessorService_;
import cds.gen.remoteservice.BusinessPartner;
import cds.gen.remoteservice.BusinessPartnerAddress_;
import cds.gen.remoteservice.BusinessPartner_;
import cds.gen.remoteservice.RemoteService;
import cds.gen.sap.capire.incidents.*;
import cds.gen.processorservice.Customers;
import cds.gen.processorservice.Customers_;
import com.sap.cds.Result;
import com.sap.cds.ResultBuilder;
import com.sap.cds.Struct;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.StructuredType;
import com.sap.cds.ql.Upsert;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CdsCreateEventContext;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsUpdateEventContext;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.sap.cds.Result;

@Component
@ServiceName(ProcessorService_.CDS_NAME)
public class ProcessorServiceHandler implements EventHandler {

  private static final Logger logger = LoggerFactory.getLogger(ProcessorServiceHandler.class);

  private final PersistenceService db;

  private final RemoteService remoteService;

  private final ApiBusinessPartner apiBusinessPartnerService;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public ProcessorServiceHandler(
      PersistenceService db, RemoteService remoteService, ApiBusinessPartner apiBusinessPartner) {
    this.db = db;
    this.remoteService = remoteService;
    this.apiBusinessPartnerService = apiBusinessPartner;
  }

  /*
   * Change the urgency of an incident to "high" if the title contains the word "urgent"
   */
  @Before(event = CqnService.EVENT_CREATE)
  public void ensureHighUrgencyForIncidentsWithUrgentInTitle(List<Incidents> incidents) {
    for (Incidents incident : incidents) {
      if (incident.getTitle().toLowerCase(Locale.ENGLISH).contains("urgent")
              && incident.getUrgencyCode() == null
          || !incident.getUrgencyCode().equals("H")) {
        incident.setUrgencyCode("H");
        logger.info("Adjusted Urgency for incident '{}' to 'HIGH'.", incident.getTitle());
      }
    }
  }

  /*
   * Handler to avoid updating a "closed" incident
   */
  @Before(event = CqnService.EVENT_UPDATE)
  public void ensureNoUpdateOnClosedIncidents(Incidents incident) {
    Incidents in =
        db.run(Select.from(Incidents_.class).where(i -> i.ID().eq(incident.getId())))
            .single(Incidents.class);
    if (in.getStatusCode().equals("C")) {
      throw new ServiceException(ErrorStatuses.CONFLICT, "Can't modify a closed incident");
    }
  }

  @On(event = CqnService.EVENT_READ, entity = Customers_.CDS_NAME)
  public Result onCustomerRead(CdsReadEventContext readContext) {

    logger.info("Delegating to S4 service...");

    Select<BusinessPartner_> select =
        Select.from(BusinessPartner_.class)
            .columns(
                StructuredType::_all,
                businessPartner ->
                    businessPartner
                        .addresses()
                        .expand(
                            businessPartnerAddress ->
                                businessPartnerAddress.expand(BusinessPartnerAddress_::email)))
            .limit(100, readContext.getCqn().skip());
    Result result = apiBusinessPartnerService.run(select);

    List<Customers> customersList =
        Struct.stream(result)
            .as(BusinessPartner.class)
            .map(
                businessPartner -> {
                  Customers customer = Customers.create();
                  customer.setId(businessPartner.getId());
                  customer.setName(businessPartner.getName());
                  if (!businessPartner.getAddresses().isEmpty()
                      && !businessPartner.getAddresses().getFirst().getEmail().isEmpty()) {
                    customer.setEmail(
                        businessPartner.getAddresses().getFirst().getEmail().getFirst().getEmail());
                  }
                  return customer;
                })
            .toList();

    return ResultBuilder.selectedRows(customersList).inlineCount(customersList.size()).result();
  }

  @On(
      event = {CqnService.EVENT_CREATE, CqnService.EVENT_UPDATE},
      entity = Incidents_.CDS_NAME)
  public Result onCustomerCache(EventContext context, List<Incidents> incidentsList) {

    // Call default event handler for Create and Update, this should handle the creation or update
    context.proceed();
    Result result =
        switch (context.getEvent()) {
          case CqnService.EVENT_CREATE -> ((CdsCreateEventContext) context).getResult();
          case CqnService.EVENT_UPDATE -> ((CdsUpdateEventContext) context).getResult();
          default -> null;
        };

    assert result != null;
    //      Incidents incident = result.single(Incidents.class);

    incidentsList
        .forEach(
            incident -> {
              // We actually only want to update the customer cache in this event handler with email
              // and phone from a remote service
              if (incident.getCustomerId() != null) {
                logger.info("CREATE or UPDATE Customer Cache...");

                Select<BusinessPartner_> select =
                    Select.from(BusinessPartner_.class)
                        .columns(
                            StructuredType::_all,
                                businessPartner ->
                                        businessPartner
                                                .addresses()
                                                .expand(
                                                        businessPartnerAddress ->
                                                                businessPartnerAddress.expand(StructuredType::_all)))
                        .where(
                            businessPartner ->
                                businessPartner.ID().eq(incident.getCustomerId()));
                BusinessPartner bupa =
                    apiBusinessPartnerService.run(select).single(BusinessPartner.class);

                if (bupa != null) {

                  Customers customer = Customers.create();
                  customer.setId(incident.getCustomerId());
                  customer.setFirstName(bupa.getFirstName());
                  customer.setLastName(bupa.getLastName());

                  if (!bupa.getAddresses().isEmpty()) {
                    if (!bupa.getAddresses().getFirst().getEmail().isEmpty()) {
                      customer.setEmail(
                          bupa.getAddresses().getFirst().getEmail().getFirst().getEmail());
                    }
                    if (!bupa.getAddresses().getFirst().getPhoneNumber().isEmpty()) {
                      customer.setPhone(
                          bupa.getAddresses().getFirst().getPhoneNumber().getFirst().getPhone());
                    }
                  }
                  db.run(Upsert.into(Customers_.class).entries(List.of(customer)));
                }
              }
            });
    return result;
  }
}
