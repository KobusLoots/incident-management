using { sap.capire.incidents as my } from '../db/schema';

/**
 * Service used by support personell, i.e. the incidents' 'processors'.
 */
service ProcessorService {
    entity Incidents as
        projection on my.Incidents {
            *,
            customerAddress : Association to many Addresses
                                  on customerAddress.customer.ID = customer.ID
        };

    @readonly
    entity Customers as projection on my.Customers;

    @readonly
    entity Addresses as projection on my.Addresses;
}

annotate ProcessorService.Incidents with @odata.draft.enabled;
annotate ProcessorService with @(requires: 'support');

/**
 * Service used by administrators to manage customers and incidents.
 */
service AdminService {
    entity Customers as projection on my.Customers;
    entity Incidents as projection on my.Incidents;
    }

annotate AdminService with @(requires: 'admin');