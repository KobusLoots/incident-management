---
title: "Add Custom Logic (Java)"
date: "July 16, 2025"
source: "https://developers.sap.com/tutorials/add-custom-logic.html"
---

# Add Custom Logic (Java)

Add business logic to your CAP **Java** backend so the UI reflects computed values and enforces rules. In this tutorial you’ll implement logic that:

- Sets **urgency = High** when a new incident title contains the word **urgent**.
- Prevents updates to incidents that are already **closed**.

## Table of Contents

1. [Prerequisites](#prerequisites)  
2. [Step 1: Add custom code](#step-1-add-custom-code)  
3. [Step 2: Understand and test the custom code](#step-2-understand-and-test-the-custom-code)  
4. [References](#references)  

---

## Prerequisites

- You’ve completed the earlier tutorials and have the Incident‑Management CAP project (Java path).  
- Your **Java** service can be started with the Maven watch goal.

Start (or restart) the backend in a terminal from the **service module**:

```bash
cd incident-management/srv
mvn cds:watch
```

> Keep this terminal running while you edit code. The watcher rebuilds and restarts the app automatically on changes.

---

## Step 1: Add custom code

Create Java event handlers to run **before CREATE** and **before UPDATE** for `ProcessorService.Incidents`.

Create the file `srv/src/main/java/customer/incident_management/handler/ProcessorServiceHandler.java` and paste:

```java
package customer.incident_management.handler;

import cds.gen.processorservice.Incidents;
import cds.gen.processorservice.ProcessorService_;
import cds.gen.sap.capire.incidents.*;
import com.sap.cds.ql.Select;
import com.sap.cds.services.ErrorStatuses;
import com.sap.cds.services.ServiceException;
import com.sap.cds.services.cds.CqnService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.Before;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Locale;

@Component
@ServiceName(ProcessorService_.CDS_NAME)
public class ProcessorServiceHandler implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProcessorServiceHandler.class);
    private final PersistenceService db;
    public ProcessorServiceHandler(PersistenceService db) {
        this.db = db;
    }
    /*
    * Change the urgency of an incident to "high" if the title contains the word "urgent"
    */
    @Before(event = CqnService.EVENT_CREATE)
    public void ensureHighUrgencyForIncidentsWithUrgentInTitle(List<Incidents> incidents) {
        for (Incidents incident : incidents) {
            if (incident.getTitle().toLowerCase(Locale.ENGLISH).contains("urgent") &&
                    incident.getUrgencyCode() == null || !incident.getUrgencyCode().equals("H")) {
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
        Incidents in = db.run(Select.from(Incidents_.class).where(i -> i.ID().eq(incident.getId()))).single(Incidents.class);
        if (in.getStatusCode().equals("C")) {
            throw new ServiceException(ErrorStatuses.CONFLICT, "Can't modify a closed incident");
        }
    }
}
```

> Notes  
> - `@ServiceName(ProcessorService_.CDS_NAME)` registers the handler for your **ProcessorService**.  
> - `@Before(CREATE)` validates/mutates incoming data; `@Before(UPDATE)` can block invalid updates.  
> - `PersistenceService` is used to look up the **current** status of an incident before allowing updates.  
> - If your package differs, adjust the `package` line and imports accordingly.

---

## Step 2: Understand and test the custom code

**What it does**  
- On **CREATE**, if `title` contains “urgent” and `urgency_code` isn’t already `'H'`, it sets it to **High**.  
- On **UPDATE**, it loads the **existing** record. If `status_code` is `'C'` (**Closed**), it throws an HTTP **409 Conflict** to prevent the modification.

### Quick test in browser

With the server running (`mvn cds:watch`), open:

- `http://localhost:8080/processor/Incidents` — use the UI/app to create and modify incidents.

### Test via `curl`

```bash
# 1) Create an incident with 'urgent' in the title; urgency should be forced to 'H'
curl -s -X POST http://localhost:8080/processor/Incidents \
  -H "Content-Type: application/json" \
  -d '{ "title": "urgent: printer down", "customer_ID":"1004155", "status_code":"N" }' | jq .

# 2) Mark an incident as closed (example ID, adjust accordingly)
INC_ID="<paste-created-id>"
curl -s -X PATCH http://localhost:8080/processor/Incidents(${INC_ID}) \
  -H "Content-Type: application/json" \
  -d '{ "status_code":"C" }'

# 3) Attempt to update a closed incident -> expect 409 Conflict
curl -i -s -X PATCH http://localhost:8080/processor/Incidents(${INC_ID}) \
  -H "Content-Type: application/json" \
  -d '{ "title":"attempt change on closed issue" }'
```

> Tip: If you don’t use `jq`, remove it or pipe the JSON elsewhere.

**Patterns** (CAP Java):  
- `@Before` to validate or enrich incoming data.  
- `@After` to post-process results (not used here).  
- Use `PersistenceService` for transactional reads when making decisions in handlers.

---

## References

- Tutorial: **Add Custom Logic** (Java tab).  
  https://developers.sap.com/tutorials/add-custom-logic.html
- CAP Java docs – **Event Handlers** and annotations (`@Before`, `@After`, `@On`).  
  https://cap.cloud.sap/docs/java/event-handlers/
- CAP Java PersistenceService.  
  https://cap.cloud.sap/docs/java/persistence
