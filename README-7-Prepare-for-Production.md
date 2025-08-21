---
title: "Prepare for Production (Java)"
date: "August 19, 2025"
source: "https://developers.sap.com/tutorials/prep-for-prod.html"
---

# Prepare for Production (Java)

Prepare your CAP **Java** application for production readiness by configuring SAP HANA Cloud, authorization, Work Zone, and running a test build.

## Table of Contents

1. [Prerequisites](#prerequisites)  
2. [Step 1: Add SAP HANA Cloud](#step-1-add-sap-hana-cloud)  
3. [Step 2: Configure Authorization and Trust Management](#step-2-configure-authorization-and-trust-management)  
4. [Step 3: Add Work Zone Configuration](#step-3-add-work-zone-configuration)  
5. [Step 4: Run a Test Build](#step-4-run-a-test-build)  
6. [Links & References](#links--references)  

---

## Prerequisites

- You’ve completed the **Add Test Cases (Java)** tutorial.  
- You are working in the **INCIDENT-MANAGEMENT** project.  

---

## Step 1: Add SAP HANA Cloud

Open a terminal at the root of your project:

```bash
cds add hana --for production
```

This command:  
- Adds a dependency (`cds-feature-hana`) to configure HANA as the production database.  
- Updates `srv/pom.xml` with the following dependency:  

```xml
<dependency>
  <groupId>com.sap.cds</groupId>
  <artifactId>cds-starter-cloudfoundry</artifactId>
</dependency>
```

Inspect the production configuration:

```bash
cds env requires -4 production
```

Example output:

```bash
{
   db: {
     impl: '@sap/cds/libx/_runtime/hana/Service.js',
     kind: 'hana',
     'deploy-format': 'hdbtable'
   },
   auth: { strategy: 'JWT', kind: 'jwt-auth', vcap: { label: 'xsuaa' } },
   approuter: { kind: 'cloud-foundry' }
 }
```

Verify the app still works locally:

```bash
cd srv
mvn cds:watch
```

Then open the service in a new browser tab.

---

## Step 2: Configure Authorization and Trust Management

Run the following command:

```bash
cds add xsuaa --for production
```

This command:  
- Adds the **SAP Authorization and Trust Management** service dependency to `srv/pom.xml`.  
- Creates the `xs-security.json` file with scopes and role templates.

### Example annotations in `services.cds`

```cds
using { sap.capire.incidents as my } from '../db/schema';

service ProcessorService  {
  ...
}
annotate ProcessorService.Incidents with @odata.draft.enabled; 
annotate ProcessorService with @(requires: 'support');

service AdminService {
  ...
}
annotate AdminService with @(requires: 'admin');
```

### Example `xs-security.json`

```json
{
  "scopes": [
    {
      "name": "$XSAPPNAME.support",
      "description": "support"
    },
    {
      "name": "$XSAPPNAME.admin",
      "description": "admin"
    }
  ],
  "attributes": [],
  "role-templates": [
    {
      "name": "support",
      "description": "generated",
      "scope-references": [
        "$XSAPPNAME.support"
      ],
      "attribute-references": []
    },
    {
      "name": "admin",
      "description": "generated",
      "scope-references": [
        "$XSAPPNAME.admin"
      ],
      "attribute-references": []
    }
  ]
}
```

---

## Step 3: Add Work Zone Configuration (Optional and not performed in this incident-management source code)

From the project root, run:

```bash
cds add workzone
```

Example output:

```bash
Adding feature 'destination'...
Adding feature 'html5-repo'...
Adding feature 'workzone'...
Adding feature 'workzone-standard'...
```

### Adjust `manifest.json`

Open `app/incidents/webapp/manifest.json` and **remove the leading slash** in the `uri`:

```json
"dataSources": {
  "mainService": {
    "uri": "odata/v4/processor/",
    "type": "OData",
    "settings": {
      "annotations": [],
      "localUri": "localService/metadata.xml",
      "odataVersion": "4.0"
    }
  }
}
```

### Install dependencies

```bash
cd db
npm install

cd ../app/incidents
npm install
```

---

## Step 4: Run a Test Build

Validate the project with:

```bash
mvn clean package
```

---

## Links & References

- Tutorial: [Prepare for Production (Java)](https://developers.sap.com/tutorials/prep-for-prod.html)  
- Related: [Using Databases](https://cap.cloud.sap/docs/guides/databases), [CAP Configuration](https://cap.cloud.sap/docs/advanced/profiles)  
