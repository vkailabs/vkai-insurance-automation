# vkai-insurance-automation

Selenium + Cucumber + TestNG BDD regression automation for the **VK AI Labs Insurance** platform.

Automates the 40-scenario Gherkin regression pack (published in Jira `VJS` as
`VJS-2`–`VJS-41`) against the **live, deployed** client and provider portals — no
local app hosting required.

| Side | Target URL | Auth |
|---|---|---|
| Client | `https://vkai-insurance-client.vercel.app` | Firebase Auth (email/password) |
| Provider | `https://vkai-insurance-provider.vercel.app` | Entra ID (Reviewer / Approver) |

## Tech stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Build | Maven |
| BDD | Cucumber-JVM 7.x |
| Test runner | TestNG (`cucumber-testng`) |
| DI | PicoContainer (`cucumber-picocontainer`) |
| UI automation | Selenium 4.x (Selenium Manager for drivers) |
| Reporting | ExtentReports (Cucumber adapter) |
| Assertions | AssertJ |
| Logging | SLF4J + Logback |

## Project structure

```
vkai-insurance-automation/
├── src/
│   ├── main/java/com/vkailabs/insurance/automation/
│   │   ├── pages/          # Page Objects (BasePage, ClientLoginPage, DashboardPage, …)
│   │   └── utils/          # ConfigReader, DriverFactory, WaitUtils
│   └── test/
│       ├── java/com/vkailabs/insurance/automation/
│       │   ├── context/         # TestContext — PicoContainer-shared WebDriver + state
│       │   ├── stepdefinitions/ # AuthSteps, … (one class per module, reusable)
│       │   ├── hooks/           # Hooks — driver lifecycle, screenshot on failure
│       │   └── runners/         # RegressionRunner (Cucumber + TestNG)
│       └── resources/
│           ├── features/        # .feature files (ported from the regression pack)
│           ├── config/          # config-local.properties(.example) — creds, tuning
│           ├── extent.properties# ExtentReports adapter config
│           └── logback-test.xml # logging config
├── reports/                # ExtentReports output (gitignored, CI artifact)
├── testng.xml              # suite: runner(s), parallel thread-count, tag groups
├── .github/workflows/      # ci.yml — Maven build + headless Chrome
└── pom.xml
```

## Build status

**Pilot stage** — the client-login vertical slice (`VJS-TC-AUTH-004` / Jira `VJS-5`) runs
green end-to-end, proving the full toolchain (Cucumber → TestNG → PicoContainer →
Selenium → ExtentReports). Remaining modules are ported on top of this foundation.

## How to run

### 1. Prerequisites

- **Java 17+** and **Maven 3.9+** (`java -version`, `mvn -version`).
- A local **Chrome** install (the default browser; Selenium Manager fetches the matching driver automatically).

### 2. Provide QA credentials

Tests read config **environment-first**, then fall back to a gitignored
`config-local.properties`. Real credentials never live in git — only the
`.example` template is committed.

Required keys:

| Key | Meaning |
|---|---|
| `VKAI_CLIENT_BASE_URL` | Client portal base URL |
| `VKAI_CLIENT_EMAIL` / `VKAI_CLIENT_PASSWORD` | Client QA account (Firebase) |
| `VKAI_PROVIDER_BASE_URL` | Provider portal base URL |
| `VKAI_PROVIDER_EMAIL` / `VKAI_PROVIDER_PASSWORD` | Provider QA account (Entra ID, Approver) |

Optional tuning (defaults shown): `VKAI_AUTOMATION_BROWSER=chrome`,
`VKAI_AUTOMATION_HEADLESS=true`, `VKAI_AUTOMATION_IMPLICIT_WAIT_SECS=0`,
`VKAI_AUTOMATION_EXPLICIT_WAIT_SECS=15`.

**Option A — environment variables** (nothing written to disk):

```bash
export VKAI_CLIENT_BASE_URL="https://vkai-insurance-client.vercel.app"
export VKAI_CLIENT_EMAIL="<client QA email>"
export VKAI_CLIENT_PASSWORD="<client QA password>"
```

**Option B — local file** (gitignored):

```bash
cp src/test/resources/config/config-local.properties.example \
   src/test/resources/config/config-local.properties
# then edit it and fill in real values
```

> Only the client keys are needed for the current pilot; the provider keys become
> required once provider/E2E scenarios are added.

### 3. Run the tests

```bash
mvn test                                 # runs the current suite (tag @Pilot)
mvn test -Dcucumber.execution.dry-run=true   # validate wiring only, no browser
VKAI_AUTOMATION_HEADLESS=false mvn test  # watch it drive a real Chrome window
```

The runner is tag-filtered (currently `@Pilot`). Widen the `tags` in
`RegressionRunner` — or add tag-specific runners in `testng.xml` — as modules land.

### 4. Reports

| Output | Path |
|---|---|
| ExtentReports (Spark HTML) | `reports/spark/index.html` |
| Cucumber HTML / JSON | `target/cucumber-reports/cucumber.html` · `.json` |

`reports/` is gitignored and published as a CI artifact. On failure, a screenshot is
attached to the scenario and embedded in the Extent report automatically.

## Conventions

- Kebab-case repo/branch/env-var naming per `VK-AI-Labs-Naming-Conventions.md`.
- Branching: `dev` integration branch, `main` protected, PRs merged manually.
