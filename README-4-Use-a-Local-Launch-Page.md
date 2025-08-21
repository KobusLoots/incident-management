---
title: "Use a Local Launch Page (Java)"
date: "July 16, 2025"
source: "https://developers.sap.com/tutorials/use-local-launch-page.html"
---

# Use a Local Launch Page (Java)

Add a **local launch page** (FLP sandbox) to your CAP Java project so you can open one or more Fiori apps from a single entry point during local development.

## Table of Contents

1. [Prerequisites](#prerequisites)  
2. [Step 1: Overview](#step-1-overview)  
3. [Step 2: Implement a local launch page](#step-2-implement-a-local-launch-page)  
4. [Step 3: Check the launchpage.html file](#step-3-check-the-launchpagehtml-file)  
5. [Notes & Limitations](#notes--limitations)  
6. [References](#references)  

---

## Prerequisites

- You’ve completed **Add Custom Logic (Java)** and have the Incident‑Management app running locally.  
- Your CAP **Java** backend can be started from the service module:

```bash
cd incident-management/srv
mvn cds:watch
```

> Keep this terminal running while testing the launch page.

---

## Step 1: Overview

So far, you open the UI directly via `app/incidents/webapp/index.html`. To launch multiple apps from one place, add a **launch page** (an HTML file that uses the SAPUI5 FLP sandbox) at the project’s `app/` level.

---

## Step 2: Implement a local launch page

1. In SAP Business Application Studio, open the **IncidentManagement** dev space (status: **RUNNING**).  
2. Create a new file **`app/launchpage.html`** (sibling to the `incidents` app folder).  
3. Paste the following minimal FLP sandbox page (adjust titles and component name if you changed them earlier):

```html
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <script>
      // FLP sandbox configuration
      window["sap-ushell-config"] = {
        defaultRenderer: "fiori2",
        services: {
          NavTargetResolution: {
            config: {
              allowTestUrlComponentConfig: true,
              enableClientSideTargetResolution: true
            }
          }
        },
        applications: {
          "incidents-app": {
            title: "Incident-Management",
            description: "Incidents",
            additionalInformation: "SAPUI5.Component=ns.incidents",
            applicationType: "URL",
            url: "./incidents/webapp",
            navigationMode: "embedded"
          }
        }
      };
    </script>
    <script src="https://ui5.sap.com/test-resources/sap/ushell/bootstrap/sandbox.js"></script>
    <script
      src="https://ui5.sap.com/resources/sap-ui-core.js"
      data-sap-ui-libs="sap.m, sap.ushell, sap.fe.templates"
      data-sap-ui-compatVersion="edge"
      data-sap-ui-theme="sap_horizon"
      data-sap-ui-frameOptions="allow"
      data-sap-ui-bindingSyntax="complex">
    </script>
    <script>
      sap.ui.getCore().attachInit(function () {
        sap.ushell.Container.createRenderer().placeAt("content");
      });
    </script>
  </head>
  <body class="sapUiBody" id="content"></body>
</html>
```

4. Make sure the **Fiori app is running** (your backend watcher is active).  
5. In the browser tab where your Fiori app is open, replace the end of the URL  
   from: `/incidents/webapp/index.html?sap-ui-xx-viewCache=false`  
   to:   `/launchpage.html#Shell-home`  
   and press **Enter** — you should now see a **tile** for *Incident‑Management* on the launch page.

---

## Step 3: Check the launchpage.html file

- The `applications` section defines one app with:  
  - `additionalInformation: "SAPUI5.Component=ns.incidents"` (the UI5 component name)  
  - `url: "./incidents/webapp"` (relative path to the app)  
  - `title`/`description` displayed on the tile  
- You can add more apps by adding entries under `applications`.  
- **Why `launchpage.html` and not `index.html`?**  
  The CAP dev server serves its own default index page at `/app/`. Keeping your file named `launchpage.html` preserves that default page.

---

## Notes & Limitations

- This sandbox page is a **local mock** of a central Launchpad/Work Zone site; it:  
  - does **not** support app add/remove via configuration,  
  - does **not** apply user roles, and  
  - does **not** include end‑user personalization.  
- For a full‑featured central site, use **SAP Build Work Zone, standard edition** (outside the scope of this local dev tutorial).
- A launch page can be added to the app router as a static resource and will work when deployed to Cloud Foundry, but the above-mentioned limitations apply. Have a look at [launchpage.html](app/router/static-resources/web-pages/launchpage.html).
---

## References

- Tutorial: **Use a Local Launch Page** (Java tab) – overview, implementation snippet, and explanation.  
  https://developers.sap.com/tutorials/use-local-launch-page.html
- SAP CAP docs: **Fiori sandbox and local testing**.  
  https://cap.cloud.sap/docs/advanced/fiori/
