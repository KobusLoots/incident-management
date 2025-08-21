---
title: "Add Test Cases (Java)"
date: "August 19, 2025"
source: "https://developers.sap.com/tutorials/add-test-cases.html"
---

# Add Test Cases (Java)

Add automated tests to your CAP **Java** application.

## Table of Contents

1. [Prerequisites](#prerequisites)  
2. [Step 1: Add dependencies](#step-1-add-dependencies)  
3. [Step 2: Add tests](#step-2-add-tests)  
4. [Step 3: Test the application](#step-3-test-the-application)  
5. [Links & References](#links--references)  

---

## Prerequisites

- You have completed the **Add Authorization (Java)** tutorial.  
- You can run your CAP Java app from the `srv` module with Maven:

```bash
cd incident-management/srv
mvn cds:watch
```

---

## Step 1: Add dependencies

Open `srv/pom.xml` and add the following dependencies inside the `<dependencies>` section:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>	
</dependency>
```

---

## Step 2: Add tests

1. Create a new folder `test` in the `srv/src` directory.  
2. Inside `srv/src/test`, create the folder structure:  

```
java/customer/incident_management
```

3. In the folder `srv/src/test/java/customer/incident_management`, create a new file named `IncidentsODataTests.java`.  

4. Add the following code:

```java
package customer.incident_management;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.hamcrest.Matchers.hasSize;
import com.jayway.jsonpath.JsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IncidentsODataTests {

  private static final String incidentsURI = "/odata/v4/ProcessorService/Incidents";
  private static final String customerURI = "/odata/v4/ProcessorService/Customers";
  private static final String expandEntityURI = "/odata/v4/ProcessorService/Customers?$select=firstName&$expand=incidents";

  @Autowired
  private MockMvc mockMvc;

  /** Test GET Api for Incidents */
  @Test
  @WithMockUser(username = "alice")
  void incidentReturned(@Autowired MockMvc mockMvc) throws Exception {
      mockMvc.perform(get(incidentsURI))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.value", hasSize(4)));
  }

  /** Test GET Api for Customers */
  @Test
  @WithMockUser(username = "alice")
  void customertReturned() throws Exception {
      mockMvc.perform(get(customerURI))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.value", hasSize(3)));
  }

  /** Ensure each Customer has an Incident */
  @Test
  @WithMockUser(username = "alice")
  void expandEntityEndpoint() throws Exception {
      mockMvc.perform(get(expandEntityURI))
          .andExpect(jsonPath("$.value[0].incidents[0]").isMap())
          .andExpect(jsonPath("$.value[0].incidents[0]").isNotEmpty());
  }

  /** Test creating, activating, and deleting a draft Incident */
  @Test
  @WithMockUser(username = "alice")
  void draftIncident() throws Exception {
      String incidentCreateJson = "{ \"title\": \"Urgent attention required!\", \"status_code\": \"N\",\"urgency_code\": \"M\"}";

      // Create draft Incident
      MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/odata/v4/ProcessorService/Incidents")
              .content(incidentCreateJson)
              .contentType("application/json")
              .accept("application/json"))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.title").value("Urgent attention required!"))
              .andExpect(jsonPath("$.status_code").value("N"))
              .andExpect(jsonPath("$.urgency_code").value("M"))
              .andReturn();

      String createResponseContent = createResult.getResponse().getContentAsString();
      String ID = JsonPath.read(createResponseContent, "$.ID");

      // Activate draft
      mockMvc.perform(MockMvcRequestBuilders.post("/odata/v4/ProcessorService/Incidents(ID=" + ID + ",IsActiveEntity=false)/ProcessorService.draftActivate")
              .contentType("application/json")
              .accept("application/json"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.urgency_code").value("H"));  

      // Delete Incident
      mockMvc.perform(MockMvcRequestBuilders.delete("/odata/v4/ProcessorService/Incidents(ID=" + ID + ",IsActiveEntity=true)"))
              .andExpect(status().is(204));
  }

  /** Test creating, closing, and preventing updates to closed Incidents */
  @Test
  @WithMockUser(username = "alice")
  void updateIncident() throws Exception {
      String incidentCreateJson = "{ \"title\": \"Urgent attention required!\", \"status_code\": \"N\", \"IsActiveEntity\": true }";
      String incidentUpdateJson = "{\"status_code\": \"C\"}";
      String closedIncidentUpdateJson = "{\"status_code\": \"I\"}";

      // Create Incident
      MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/odata/v4/ProcessorService/Incidents")
              .content(incidentCreateJson)
              .contentType("application/json")
              .accept("application/json"))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.status_code").value("N"))
              .andReturn();

      String createResponseContent = createResult.getResponse().getContentAsString();
      String ID = JsonPath.read(createResponseContent, "$.ID");

      // Close Incident
      mockMvc.perform(MockMvcRequestBuilders.patch("/odata/v4/ProcessorService/Incidents(ID=" + ID + ",IsActiveEntity=true)")
              .content(incidentUpdateJson)
              .contentType("application/json")
              .accept("application/json"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.status_code").value("C"));

      // Attempt update after closed
      mockMvc.perform(MockMvcRequestBuilders.patch("/odata/v4/ProcessorService/Incidents(ID=" + ID + ",IsActiveEntity=true)")
              .content(closedIncidentUpdateJson)
              .contentType("application/json")
              .accept("application/json"))
              .andExpect(status().isConflict())
              .andExpect(jsonPath("$.error.message").value("Can't modify a closed incident"));
  }
}
```

---

## Step 3: Test the application

Run the following command in the terminal:

```bash
mvn verify
```

---

## Links & References

- Tutorial: [Add Test Cases (Java)](https://developers.sap.com/tutorials/add-test-cases.html)  
