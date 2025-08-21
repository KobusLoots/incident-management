---
title: "Build a CAP Application (Java)"
date: "June 18, 2025"
source: "https://developers.sap.com/tutorials/build-cap-app.html"
---

# Build a CAP Application (Java)

A step-by-step guide to building a CAP (Cloud Application Programming Model) application using **Java**, with shared steps applicable to both Java and Node.js.

## Table of Contents

1. [Prerequisites](#prerequisites)  
2. [Step 1: Create a CAP Project (Java)](#step-1-create-a-cap-project-java)  
3. [Step 2: Add a Domain Model](#step-2-add-a-domain-model)  
4. [Step 3: Create Services (Java)](#step-3-create-services-java)  
5. [Step 4: Generate CSV Templates](#step-4-generate-csv-templates)  
6. [Step 5: Fill in the Test Data](#step-5-fill-in-the-test-data)  
7. [Query the OData Endpoints](#query-the-odata-endpoints)  

---

## Prerequisites

You must have SAP Business Application Studio configured. The setup is described in the ["Set Up SAP Business Application Studio for Development"](https://developers.sap.com/tutorials/set-up-business-application-studio.html).

If the `cds` command isn't available, install the CAP CLI tool (cds-dk) globally and restart your terminal:

```bash
npm i -g @sap/cds-dk
```

---

## Step 1: Create a CAP Project (Java)

Create a new project, open it, and start the Java server in **watch** mode:

```bash
cds init incident-management --add java
cd incident-management/srv
mvn cds:watch
```

> **Notes**
> - Run `mvn cds:watch` inside your **service module** (`./srv`), where the Spring Boot app lives.  
> - `cds:watch` watches your CDS model and restarts automatically on changes. Use **Ctrl+C** to stop.

---

## Step 2: Add a Domain Model

In the `db` folder, create a new file named `schema.cds` and paste the following CDS model:

```cds
using { cuid, managed, sap.common.CodeList } from '@sap/cds/common';
namespace sap.capire.incidents;

entity Incidents : cuid, managed {
  customer     : Association to Customers;
  title        : String  @title : 'Title';
  urgency      : Association to Urgency default 'M';
  status       : Association to Status default 'N';
  conversation : Composition of many {
    key ID        : UUID;
    timestamp     : type of managed:createdAt;
    author        : type of managed:createdBy;
    message       : String;
  };
}

entity Customers : managed {
  key ID        : String;
  firstName     : String;
  lastName      : String;
  name          : String = firstName || ' ' || lastName;
  email         : EMailAddress;
  phone         : PhoneNumber;
  incidents     : Association to many Incidents on incidents.customer = $self;
  creditCardNo  : String(16) @assert.format: '^[1-9]\\d{15}$';
  addresses     : Composition of many Addresses on addresses.customer = $self;
}

entity Addresses : cuid, managed {
  customer       : Association to Customers;
  city           : String;
  postCode       : String;
  streetAddress  : String;
}

entity Status : CodeList {
  key code: String enum {
    new        = 'N';
    assigned   = 'A';
    in_process = 'I';
    on_hold    = 'H';
    resolved   = 'R';
    closed     = 'C';
  };
  criticality : Integer;
}

entity Urgency : CodeList {
  key code: String enum {
    high   = 'H';
    medium = 'M';
    low    = 'L';
  };
}

type EMailAddress   : String;
type PhoneNumber    : String;
```

---

## Step 3: Create Services (Java)

In the `srv` folder, create a new file named `services.cds` and define your services:

```cds
using { sap.capire.incidents as my } from '../db/schema';

/** Used by support team members to process incidents */
service ProcessorService {
  @readonly entity Customers as projection on my.Customers;
  entity Incidents as projection on my.Incidents;
}

/** Used by administrators for admin activities */
service AdminService {
  entity Customers as projection on my.Customers;
  entity Incidents as projection on my.Incidents;
}
```

---

## Step 4: Generate CSV Templates

Generate initial data templates with:

```bash
cds add data
```

This creates CSV files for entities like Addresses, Customers, Incidents, etc., in the `db/data` folder.

---

## Step 5: Fill in the Test Data

Replace the generated CSV templates with actual sample data, e.g.:

**db/data/sap.capire.incidents-Addresses.csv**

```
ID,customer_ID,city,postCode,streetAddress
17e00347-dc7e-4ca9-9c5d-06ccef69f064,1004155,Rome,00164,Piazza Adriana
d8e797d9-6507-4aaa-b43f-5d2301df5135,1004161,Munich,80809,Olympia Park
ff13d2fa-e00f-4ee5-951c-330f490777b,1004100,Walldorf,69190,Dietmar-Hopp-Allee
```

**db/data/sap.capire.incidents-Customers.csv**

```
ID,firstName,lastName,email,phone
1004155,Daniel,Watts,daniel.watts@demo.com,+39-555-123
1004161,Stormy,Weathers,stormy.weathers@demo.com,+49-020-022
1004100,Sunny,Sunshine,sunny.sunshine@demo.com,+49-555-789
```

…and so on for Incidents, Status, and Urgency.

---

## Query the OData Endpoints

After running `mvn cds:watch`, you can query:

- [http://localhost:8080/processor/Customers](http://localhost:8080/processor/Customers)  
- [http://localhost:8080/processor/Incidents](http://localhost:8080/processor/Incidents)  
- [http://localhost:8080/admin/Customers](http://localhost:8080/admin/Customers)  
- [http://localhost:8080/admin/Incidents](http://localhost:8080/admin/Incidents)  

Use OData query options like `$filter`, `$expand`, and `$select`.  

---
