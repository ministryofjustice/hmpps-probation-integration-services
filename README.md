# Probation Integration Services

[![Repository Standards](https://img.shields.io/badge/dynamic/json?color=blue&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-probation-integration-services)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-probation-integration-services "Link to report")
[![Trivy](https://github.com/ministryofjustice/hmpps-probation-integration-services/actions/workflows/security.yml/badge.svg)](https://github.com/ministryofjustice/hmpps-probation-integration-services/actions/workflows/security.yml)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ministryofjustice_hmpps-probation-integration-services&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=ministryofjustice_hmpps-probation-integration-services)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ministryofjustice_hmpps-probation-integration-services&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=ministryofjustice_hmpps-probation-integration-services)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ministryofjustice_hmpps-probation-integration-services&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ministryofjustice_hmpps-probation-integration-services)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ministryofjustice_hmpps-probation-integration-services&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ministryofjustice_hmpps-probation-integration-services)


A collection of small, domain-focused integrations to support HMPPS Digital services that need to interact with 
probation data. Typically, these integration services will perform translations between HMPPS and Delius domain 
concepts, and are responsible for:
* publishing REST endpoints to read existing data from the Delius database
* listening for HMPPS Domain Event messages and writing data into the Delius database

# Project
## Goals
This project is intended to reduce the surface area of larger integration systems (e.g. [Community API](https://github.com/ministryofjustice/community-api)),
and to replace other components (e.g. [Case Notes to Probation](https://github.com/ministryofjustice/case-notes-to-probation))
with simpler services that have direct access to the Delius database.

With this in mind, we aim to:
* Support HMPPS Digital teams by building and deploying at pace
* Separate overlapping domain concepts by creating smaller, more focused services
* Simplify the developer experience by unifying common approaches and streamlining workflows

## Design Decisions
* Each service serves one, and only one, client
* Services have a single well-defined purpose
* Entities/projections are defined using domain-specific language (e.g. CaseNote, not Contact)

A full list of decision records can be found in [decisions](doc/adr)

## Tooling
* Code is written in [Kotlin](https://kotlinlang.org/), using [Spring Boot](https://spring.io/projects/spring-boot)
* Built and tested as a multi-project [Gradle](https://gradle.org/) build
* Unit tests with [JUnit](https://junit.org/) and [Mockito](https://mockito.org/)
* Integration tests with [Wiremock](https://wiremock.org/), [H2](https://www.h2database.com/), and [embedded ActiveMQ](https://activemq.apache.org/)
* End-to-end testing with [Playwright](https://playwright.dev/) - see [End-to-end tests](https://github.com/ministryofjustice/hmpps-probation-integration-services#end-to-end-tests)
* Container images are built with [Jib](https://github.com/GoogleContainerTools/jib#readme), and pushed to 
[GitHub Packages](https://github.com/orgs/ministryofjustice/packages?repo_name=hmpps-probation-integration-services)
* Code formatting by [ktlint](https://ktlint.github.io/)
* Continuous integration with [GitHub Actions](https://help.github.com/en/actions)

# Development
The project is configured to enable developers to build/test/run integration services in isolation without the need for 
Docker or remote dependencies.

To set up your development environment,
1. Open the project in [IntelliJ IDEA](https://www.jetbrains.com/idea/)
2. To run tests for a service, right-click the `src/test` folder in the project view and select "Run tests".  See [Test](#test).
3. To start the service, use the pre-defined run configuration in `.idea/runConfigurations` (See [Run](#run)).

## Code formatting
Kotlin code is formatted using [ktlint](https://ktlint.github.io/). IntelliJ will detect the ktlint configuration in 
[.idea/codeStyles](.idea/codeStyles) and [.idea/inspectionProfiles](.idea/inspectionProfiles) and ensure formatting is 
applied consistently in your IDE.

To fix any formatting issues in your code locally, run 
```shell
./gradlew ktlintFormat
```

Or, to add a pre-commit hook to automatically fix any formatting issues, run:
```shell
./gradlew addKtlintFormatGitPreCommitHook
```

# Build
IntelliJ will automatically build your code as needed. Any tasks you run from the root project, without specifying a 
project name will be run on all the children. To build the entire repository using Gradle, run:
```shell
./gradlew build
```

To build just a specific project.
```shell
./gradlew <project-name>:build
```

Use buildDependents to build and test all projects that depend on a given project (for instance a shared library)
```shell
./gradlew <project-name>:buildDependents
```

## Docker
To build Docker images and push to your local repository, run:
```shell
./gradlew <project-name>:jibDockerBuild
```

# Run
## IntelliJ
In IntelliJ IDEA, a [run configuration](https://www.jetbrains.com/help/idea/run-debug-configuration.html) is available 
for each service. Select it from the toolbar, and click either Run or Debug. The service will start in the `dev` 
profile, which typically auto-configures embedded test data and services.

Run configurations are stored in [.idea/runConfigurations](.idea/runConfigurations).

## Gradle
To run Gradle tasks in a sub-project, prepend the task name with the name of the project. Environment variables can be 
used to set the dev profile. For example,
```shell
SPRING_PROFILES_ACTIVE=dev ./gradlew <project-name>:bootRun
```

# Test
## Integration tests
Integration tests use WireMock JSON files to mock any external services.

The json files for simulations must reside in simulations/mappings in the dev class path. This makes them usable for dev and test.
Any json bodies to add to mappings must live in the simulations/__files directory. These are the defaults for WireMock.
Any json mappings and body files provided in these locations will be automatically loaded and available during dev and test.

The WireMock Server is exposed as a spring bean and can be injected into Spring Boot (Integration) Tests
for verification or adding extra scenarios specific to a test that are not available in json.

```
@Autowired
private val wireMockServer: WireMockServer
```

The strategy with the dev/test profiles is to use a single WireMock server and distinguish any potential duplicate urls using a service name on the url if required.
For example if two urls were used as part of a service 

```
https://hmpps.service1/offender/{crn}
https://hmpps.service2/offender/{crn}
```

When mocking these urls the following would be appropriate (rather than a separate mock server for each service)
```
{wiremockUrl}:{wiremockPort}/service1/offender/{crn}
{wiremockUrl}:{wiremockPort}/service2/offender/{crn}
```

All other concepts of Spring Boot Tests are usable as per Spring documentation.

## End-to-end tests

End-to-end tests are written in [TypeScript](https://www.typescriptlang.org/) using [Playwright](https://playwright.dev/),
in the [hmpps-probation-integration-e2e-tests](https://github.com/ministryofjustice/hmpps-probation-integration-e2e-tests) 
repository.

We run the end-to-end tests in GitHub Actions as part of the 
[deployment pipeline](https://github.com/ministryofjustice/hmpps-probation-integration-services/actions/workflows/pipeline.yml), 
against a real dev/test environment with all dependencies.  Running the tests in a real environment gives us confidence 
that all the integration points involved in the user journey are working correctly before we push changes to production.

You can also run the end-to-end tests from your branch to get early feedback, by following the instructions here: [Manually running a workflow](https://docs.github.com/en/actions/managing-workflow-runs/manually-running-a-workflow).
Note: this will deploy your dev code to the test and preprod environments.

To access internal services from GitHub Actions, we use a repository-level [self-hosted runner](https://docs.github.com/en/actions/hosting-your-own-runners/about-self-hosted-runners)
in MOJ Cloud Platform.
See [06-github-actions-runner.yaml](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration/06-github-actions-runner.yaml).
For more information on how this is implemented, see [PI-340](https://dsdmoj.atlassian.net/browse/PI-340).

# Deployment
Once the code is built and tested, GitHub Actions deploys the updated images for each service to an Amazon Elastic 
Container Service (ECS) cluster in the Delius AWS account. Deploying the services to the Delius AWS account enables 
secure access to the Delius database.

For documentation on the Delius ECS cluster, see the [ECS Cluster Confluence page](https://dsdmoj.atlassian.net/wiki/spaces/DAM/pages/3107979730/ECS+Cluster).
The infrastructure code for the ECS services can be found in the [hmpps-delius-core-terraform](https://github.com/ministryofjustice/hmpps-delius-core-terraform/tree/main/application/probation-integration-services) 
repository.

## Environments
Although the services are deployed to the Delius environments, they typically need to interact with resources in MOJ 
Cloud Platform.

We map Delius environments to MOJ Cloud Platform namespaces as follows:

| Delius          | MOJ Cloud Platform | Used for               |
|-----------------|--------------------|------------------------|
| delius-test     | dev                | End-to-end testing     |
| delius-pre-prod | preprod            | Testing with live data |
| delius-prod     | prod               | Live service           |

## Configuration
Each subproject has a `deploy` folder containing YAML files used for configuration.
The standard `values.yml` file provides common configuration across all environments, while additional files (e.g. 
`values-dev.yml`) can be used to set environment-specific configuration.

There is also a repository-level [defaults.yml](templates/defaults.yml) containing the default configuration across all 
projects.

```bash
├── templates
│   └── defaults.yml                      # default values across all projects
└── projects
    └── workforce-allocations-to-delius
        └── deploy
            ├── values.yml                # common values across each environment
            └── values-<environment>.yml  # 1 per environment
```

### Setting project wide values

`<project>/deploy/values.yml`

This file contains values that are the same across all environments.
Example:

```yaml
# Image
image:
  name: project-name

# Container resources
# See https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definition_parameters.html#ContainerDefinition-taskcpu
limits:
  cpu: 1024    # = 1 vCPU
  memory: 1024 # = 1 GB

# Environment variables
env:
  JAVA_OPTS: "-Xmx512m"
  SERVER_PORT: "8080"
  SPRING_PROFILES_ACTIVE: "my-profile"

# Secrets
# These are stored in the AWS Parameter Store, and referenced by their name
secrets:
  CLIENT_SECRET: /parameters/client-secret
```

### Setting environment specific values

`<project>/deploy/values-<environment>.yml`

This file should only contain values that differ between environments.
Additionally, it must specify the Delius environment in the `environment_name` value.

Example:

```yaml
environment_name: delius-pre-prod

memory: 2048

env:
  SERVICE_URL: https://example.com
```

### Secrets
Add secrets for each environment here: https://github.com/ministryofjustice/hmpps-probation-integration-services/settings/secrets/actions.

The deployment job pushes GitHub secrets to AWS Parameter Store. 
Then at runtime, ECS passes these secrets from AWS Parameter Store as environment variables to the container.

GitHub secret names should be uppercase and prefixed with the project name. (e.g. `PRISON_CASE_NOTES_TO_PROBATION_CLIENT_ID`).
When the secrets are pushed to parameter store, their names will be converted to paths (e.g. `prison-case-notes-to-probation/client-id`),
which is how they should be referenced in the `values*.yml` files.

For more details, see the "Add secrets to parameter store" step in [deploy.yml](.github/workflows/deploy.yml).

## Accessing the Delius Database
To configure access to the Delius probation database, add an `access.yml` file to the project's `deploy/database` 
folder.

The `access.yml` file defines the account used for accessing the database, as well as an optional user for auditing
interactions.  Example (see [access.yml](projects/prison-case-notes-to-probation/deploy/database/access.yml):
```yaml
database:
  access:
    username_key: /prison-case-notes-to-probation/db-username    # references AWS Parameter Store 
    password_key: /prison-case-notes-to-probation/db-password    # (see Secrets section above)
    tables:
      # A list of tables that the service can write to. Read access is granted on all tables.
      - audited_interaction
      - contact

  audit:
    username: PrisonCaseNotesToProbation
    forename: Prison Case Notes
    surname: Service
```

Before each deployment, GitHub Actions will invoke a pre-defined [Systems Manager Automation Runbook](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-automation.html)
in AWS to create/update the access account and the audit user in the Delius database.  The runbook is in the 
[hmpps-delius-pipelines](https://github.com/ministryofjustice/hmpps-delius-pipelines/tree/master/components/oracle/playbooks/probation_integration_access) 
repository.

## Accessing MOJ Cloud Platform
To access SQS queues or other AWS resources in MOJ Cloud Platform, add an IAM policy to [cloud-platform-environments](https://github.com/ministryofjustice/cloud-platform-environments)
that grants access to one of the following roles:
* Dev/Test: `arn:aws:iam::728765553488:role/delius-test-ecs-sqs-consumer`
* Pre-Prod: `arn:aws:iam::010587221707:role/delius-pre-prod-ecs-sqs-consumer`
* Production: `arn:aws:iam::050243167760:role/delius-prod-ecs-sqs-consumer`

Example: [case-notes-sub-queue.tf](https://github.com/ministryofjustice/cloud-platform-environments/blob/7a028911f8ed459a30e98d8dbba8cdcf7283ac93/namespaces/live.cloud-platform.service.justice.gov.uk/offender-events-dev/resources/case-notes-sub-queue.tf#L42-L57).

To access HTTP endpoints in MOJ Cloud Platform, the following IP ranges should be added to their allow list:
```yaml
# values-dev.yml
  allowlist:
    delius-test-1: "35.176.126.163/32"
    delius-test-2: "35.178.162.73/32"
    delius-test-3: "52.56.195.113/32"
--- 
# values-preprod.yml
  allowlist:
    delius-pre-prod-1: "52.56.240.62/32"
    delius-pre-prod-2: "18.130.110.168/32"
    delius-pre-prod-3: "35.178.44.184/32"
--- 
# values-prod.yml
  allowlist:
    delius-prod-1: "52.56.115.146/32"
    delius-prod-2: "35.178.104.253/32"
    delius-prod-3: "35.177.47.45/32"
```

# Support
For any issues or questions, please contact the Probation Integration team via the [#probation-integration-tech](https://mojdt.slack.com/archives/C02HQ4M2YQN)
Slack channel. Or feel free to create a [new issue](https://github.com/ministryofjustice/hmpps-probation-integration-services/issues/new) 
in this repository.
