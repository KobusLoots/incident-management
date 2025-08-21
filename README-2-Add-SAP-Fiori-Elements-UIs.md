---
title: "Add SAP Fiori Elements UIs (Java)"
date: "March 7, 2025"
source: "https://developers.sap.com/tutorials/add-fiori-elements-uis.html"
---

# Add SAP Fiori Elements UIs (Java)

Enhance your CAP **Java** application by adding SAP Fiori elements UIs on top of your OData V4 service.

## Table of Contents

1. [Prerequisites](#prerequisites)  
2. [Step 1: Overview](#step-1-overview)  
3. [Step 2: Generate the UI with a Fiori Elements Template](#step-2-generate-the-ui-with-a-fiori-elements-template)  
4. [Step 3: Start the Incident-Management Application](#step-3-start-the-incident-management-application)  
5. [Step 4: Configure the List View Page](#step-4-configure-the-list-view-page)  
6. [Step 5: Configure the Incident Object Page](#step-5-configure-the-incident-object-page)  
7. [Step 6: Enable Draft with @odata.draft.enabled](#step-6-enable-draft-with-odatadraftenabled)  

---

## Prerequisites

- You have a running **CAP Java** project (for example, Tutorial 1: *Build a CAP Application (Java)*).  
- SAP Business Application Studio (BAS) is set up and your **IncidentManagement** dev space is **RUNNING**.

---

## Step 1: Overview

SAP Fiori elements provides preconfigured UI floorplans based on **annotations** and service **metadata**. At runtime, SAPUI5 interprets these to render the app—no custom UI controllers needed.

---

## Step 2: Generate the UI with a Fiori Elements Template

1. In BAS, open your **IncidentManagement** dev space.  
2. Open the **Command Palette** (`Ctrl` + `Shift` + `P` / `Cmd` + `Shift` + `P`) → run **Fiori: Open Application Generator**.  
3. **Template Selection**: choose **List Report Page** → **Next**.  
4. **Data Source & Service**:  
   - Data source: **Use a Local CAP Project**  
   - CAP project: `incident-management`  
   - OData service: **ProcessorService (Java)** → **Next**  
5. **Entity Selection**:  
   - Main entity: **Incidents**  
   - Navigation entity: **None**  
   - Add table columns automatically: **Yes**  
   - Table Type: **Responsive** → **Next**  
6. **Project Attributes**:  
   - Module name: `incidents`  
   - Application title: `Incident-Management`  
   - Application namespace: `ns`  
   - Leave other defaults → **Finish**  

The generator creates an app module (for example, `app/incidents`) with a minimal `webapp/` whose logic is provided by `sap/fe/core/AppComponent`.

---

## Step 3: Start the Incident-Management Application

Open a terminal and run the **CAP Java** server from the **service module**:

```bash
# from your project root
cd incident-management/srv
mvn cds:watch
```

- `mvn cds:watch` starts the CAP **Java** app and automatically rebuilds/restarts on CDS changes.  
- Keep this terminal running while you preview and edit the Fiori app.

> If you prefer the long form, you can also use `mvn com.sap.cds:cds-maven-plugin:watch`.

---

## Step 4: Configure the List View Page

**Open the page editor**  
1. In **Application Info – incidents**, choose **Open Page Map**.  
2. In the **List Report** tile, click the **pencil** to open the Page Editor.

**Add filter fields**  
1. In **Filter Bar → Filter Fields** → **+** → **Add Filter Fields**.  
2. Add **`status_code`** and **`urgency_code`**.  
3. Update labels and **generate i18n keys** (globe icon): *Status*, *Urgency*.  
4. For both filters set **Display Type = Value Help** → in the popup set **Value Description Property = `descr`**.

**Adjust columns**  
1. Delete columns: `customer_ID`, `urgency_code`, `status_code`.  
2. **Add Basic Columns**: `status/descr`, `urgency/descr`, `customer/name`.  
3. Move `customer/name` just under **Title**.  
4. Set i18n labels for **Customer**, **Status**, **Urgency**.  
5. In **Table → Initial Load**, select **Enabled**; **Type** = **ResponsiveTable**.  
6. In **Table → Columns → Status**, set **Criticality = `status/criticality`**.

---

## Step 5: Configure the Incident Object Page

**Edit header**  
1. In **Page Map**, click the **Incident Object Page** pencil.  
2. **Header**:  
   - **Title** = `title`  
   - **Description Type** = **Property** → choose `customer/name` → **Apply**  
   - **Icon URL** = `sap-icon://alert`

**Add “Overview” section**  
1. **Sections** → **+** → **Add Group Section**.  
2. Label: **Overview** (generate an **i18n** text key).

**Add “Details” subsection**  
1. **Sections → Overview → Subsections** → **+** → **Add Form Section**.  
2. Label: **Details** (generate **i18n** key).

**Configure fields**  
1. In **Sections → General Information**, generate an **i18n** text key for the section label.  
2. **General Information → Form → Fields**: delete `urgency_code` and `status_code`.  
3. For **customer_ID**: move under **Title**; label it **Customer** (generate **i18n**).  
4. For **Customer**: set **Text** = `customer/name`, **Text Arrangement** = **Text Only**.  
   - In **Value Help** popup:  
     - **Value Description Property** = **None**  
     - **Display as Dropdown** = **Off**  
     - **Result List** → **Add Column**: `name`, `email` → **Apply**.  
5. Drag **General Information** under **Overview → Subsections**.  
6. In **Overview → Subsections → Details → Form → Fields** → **+** → **Add Basic Fields**: add `status_code`, `urgency_code`.  
7. For **Status**: **Text** = `status/descr`, **Display Type** = **Value Help** → set **Value Source Entity** = **Status**, **Value Source Property** = **code**, **Value Description Property** = **descr** → **Apply**.  
8. For **Urgency**: **Text** = `urgency/descr`, **Display Type** = **Value Help** → set **Value Source Entity** = **Urgency**, **Value Source Property** = **code**, **Value Description Property** = **descr** → **Apply**.

**Add “Conversation” table section**  
1. **Sections** → **+** → **Add Table Section**.  
2. Label: **Conversation** (generate **i18n**).  
3. In popup set **Source Value** = `conversation` → **Add**.

**Configure Conversation columns & table**  
1. **Conversation → Table → Columns** → **+** → **Add Basic Columns**: add `author`, `message`, `timestamp`.  
2. Relabel to **Author**, **Message**, **Date** (generate **i18n** keys).  
3. **Table**: **Type** = **ResponsiveTable**; **Creation Mode: Name** = **Inline**.

---

## Step 6: Enable Draft with `@odata.draft.enabled`

In `srv/services.cds` add the draft annotation for the **Incidents** entity provided by **ProcessorService**:

```cds
service ProcessorService {
  // ...
}

annotate ProcessorService.Incidents with @odata.draft.enabled;
```

**Try it**  
1. Start creating a new incident but leave **Customer**, **Status**, and **Urgency** empty.  
2. Go back to the list view without saving — a **draft** entry appears.  
3. Open it again to continue editing from where you stopped.

---

## Links & References

- Tutorial: [Add SAP Fiori Elements UIs](https://developers.sap.com/tutorials/add-fiori-elements-uis.html)  
- CAP Java watch goal: [`mvn cds:watch`](https://cap.cloud.sap/docs/java/assets/cds-maven-plugin-site/watch-mojo.html)  
- CAP docs: [Getting Started in a Nutshell](https://cap.cloud.sap/docs/get-started/in-a-nutshell)
