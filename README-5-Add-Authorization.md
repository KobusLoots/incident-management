---
title: "Add Authorization (Java)"
date: "July 16, 2025"
source: "https://developers.sap.com/tutorials/add-authorization.html"
---

# Add Authorization (Java)

Enable authentication and authorization for your CAP **Java** application.

## Table of Contents

1. [Prerequisites](#prerequisites)  
2. [Step 1: Add CAP role restrictions to services](#step-1-add-cap-role-restrictions-to-services)  
3. [Step 2: Enable auth locally (dependency + mock users)](#step-2-enable-auth-locally-dependency--mock-users)  
4. [Step 3: Access the application](#step-3-access-the-application)  
5. [Notes & Tips](#notes--tips)  
6. [Links & References](#links--references)  

---

## Prerequisites

- You’ve completed the previous tutorials and can run the app from the **service module**:
  
  ```bash
  cd incident-management/srv
  mvn cds:watch
  ```

---

## Step 1: Add CAP role restrictions to services

Open `srv/services.cds` and add **requires** annotations to your services (Java path only):

```cds
using { sap.capire.incidents as my } from '../db/schema';

/** Used by support team members to process incidents */
service ProcessorService {
  // entities...
}
annotate ProcessorService.Incidents with @odata.draft.enabled;
annotate ProcessorService with @(requires: 'support');

/** Used by administrators for admin activities */
service AdminService {
  // entities...
}
annotate AdminService with @(requires: 'admin');
```

**Effect**  
- Users with role **support** can access **ProcessorService**.  
- Users with role **admin** can access **AdminService**.

---

## Step 2: Enable auth locally (dependency + mock users)

The authorization checks in the CAP model apply when deployed **and** when running locally. To test locally you must enable authn/z and define mock users.

### 2.1 Add runtime dependency (srv/pom.xml)

Add the **Cloud Foundry starter** dependency under the `<dependencies>` section to enable authentication/authorization at runtime. Without it, any call to a `@requires`-annotated service fails with an authentication error.

```xml
<!-- srv/pom.xml -->
<dependency>
  <groupId>com.sap.cds</groupId>
  <artifactId>cds-starter-cloudfoundry</artifactId>
</dependency>
```

> After changing dependencies, **restart** your watcher (stop and re-run `mvn cds:watch`) to ensure Maven picks up the update.

### 2.2 Define mock users (srv/src/main/resources/application.yaml)

Add mock users via standard Spring Boot configuration. Use a **map** of users, each with a set of roles; **no passwords** are required for this tutorial path.

```yaml
# srv/src/main/resources/application.yaml
cds:
  security:
    mock.users:
      alice:
        roles: [ admin, support ]
      bob:
        roles: [ support ]
```

You’ve now defined two users:
- **alice** — roles: `admin`, `support`
- **bob** — role: `support`

---

## Step 3: Access the application

1. Ensure the server is running with the updated dependency:
   ```bash
   cd incident-management/srv
   mvn cds:watch
   ```
2. Open your local launch page (from the earlier tutorial) and click the **Incident‑Management** tile:
   - `http://localhost:8080/launchpage.html#Shell-home`
3. When asked to authenticate, use one of the configured usernames:
   - **alice** (no password) → has **admin + support** access.
   - **bob** (no password) → has **support** access.
4. To switch users, close all browser windows (or restart the browser) and sign in with the other username.

---

## Notes & Tips

- If every request is rejected with **401 Unauthorized**, confirm the **`cds-starter-cloudfoundry`** dependency is present and that you **restarted** `mvn cds:watch` after editing `pom.xml`.
- If there is no login prompt, verify the `application.yaml` path and indentation, and that `mock.users` is under `cds.security`.
- For finer-grained rules (entity/operation), complement service-level `@(requires: ...)` with `@restrict` in CDS or checks in event handlers.

---

## Links & References

- Tutorial: **Add Authorization** (Java tab) — explains dependency and mock user configuration for local testing.  
  https://developers.sap.com/tutorials/add-authorization.html
- CAP Java security: mock users and runtime authorization.  
  https://cap.cloud.sap/docs/java/security
