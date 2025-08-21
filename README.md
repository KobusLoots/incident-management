### Incident Management (CAP Java) – Learning Path

This repository contains a step-by-step learning path to build an Incident Management app with SAP Cloud Application Programming Model (CAP) using Java. Each numbered README walks you through one stage, from creating the CAP project to preparing it for production.

Use the navigation below to open each step and see a brief description of what it covers.

---

### Navigation

1) Build a CAP Application (Java)
- [README-1-Build-a-CAP-Application.md](README-1-Build-a-CAP-Application.md)
- Summary: Initialize a CAP Java project, define the domain model (schema.cds), expose services, generate CSV templates, load sample data, and run the backend with mvn cds:watch. Learn how to query OData endpoints of ProcessorService and AdminService locally.

2) Add SAP Fiori Elements UIs (Java)
- [README-2-Add-SAP-Fiori-Elements-UIs.md](README-2-Add-SAP-Fiori-Elements-UIs.md)
- Summary: Generate a Fiori elements List Report app for ProcessorService.Incidents. Configure filter bar, table columns, the object page (header, sections, value helps), and enable draft editing via @odata.draft.enabled.

3) Add Custom Logic (Java)
- [README-3-Add-Custom-Logic.md](README-3-Add-Custom-Logic.md)
- Summary: Implement CAP Java event handlers. Automatically set urgency to High when titles include "urgent", and block updates to incidents with status Closed. Includes quick browser and curl-based testing tips.

4) Use a Local Launch Page (Java)
- [README-4-Use-a-Local-Launch-Page.md](README-4-Use-a-Local-Launch-Page.md)
- Summary: Add a local FLP sandbox launch page (launchpage.html) so you can open one or more Fiori apps from a single entry point during local development.

5) Add Authorization (Java)
- [README-5-Add-Authorization.md](README-5-Add-Authorization.md)
- Summary: Protect services with @(requires: ...) in CDS, enable authentication/authorization locally via cds-starter-cloudfoundry, and configure mock users in application.yaml (e.g., alice and bob) to test role-based access.

6) Add Test Cases (Java)
- [README-6-Add-Test-Cases.md](README-6-Add-Test-Cases.md)
- Summary: Add test dependencies and implement MockMvc-based integration tests for OData endpoints, draft activation, and enforcing business rules (e.g., preventing updates to closed incidents). Execute tests with mvn verify.

7) Prepare for Production (Java)
- [README-7-Prepare-for-Production.md](README-7-Prepare-for-Production.md)
- Summary: Add SAP HANA Cloud for production, configure Authorization & Trust Management (xsuaa) and role templates, optionally add Work Zone integration, and validate with a production build (mvn clean package).

---

### How to get started locally

- Backend (Java):
    - From the service module: cd incident-management/srv && mvn cds:watch
- UI (Fiori elements):
    - Follow Step 2 to generate the app (default path: app/incidents).
    - Optional: Use the local launch page from Step 4 at http://localhost:8080/launchpage.html#Shell-home

### Suggested order

Complete the steps in numerical order (1 → 7). Each step builds on the previous one.

### Repository pointers

- db/ — CDS model and sample data (CSV) templates
- srv/ — CAP Java service, handlers, and configuration
- app/incidents — Fiori elements UI module

If you’re new to CAP, start with Step 1 and use this README to navigate through the rest of the tutorials.

## Changelog

| Version        | Change | Comments |
|----------------|--------|----------|
| 1.1.1-SNAPSHOT |        | Publish  |
