# 0007 - Bootstrap New Projects Using GitHub Actions

## Status

Accepted

## Context

The monorepo for HMPPS Probation Integration Services contains multiple
sub-projects with a number of shapes based on the type of integration we are
supporting.

Currently the integration behaviours we need to support can be classified as
one of:

- An API to gather information from the source systems
- A message consumer to write information to the source systems
- A combination of API and message consumer
- A COTS product to fulfil some necessary operation

As we are partitioning integration services by HMPPS Domain and scoping them
to individual systems we expect the number of projects within the repository
to grow. As we take responsibility for more integration activity we have an
ongoing need to create new services within the repository.

It is possible to define skeleton structures for each of these different
project shapes with a small number of parameters to control project creation.

There are currently a small number of manual steps needed to create a new
project which need to be actioned before the project setup is complete.


## Decision

- We will use a GitHub Actions [workflow](<https://github.com/ministryofjustice/hmpps-probation-integration-services/tree/main/.github/workflows/bootstrap.yml>)
  to bootstrap projects in this repository
- The workflow will submit a pull request to this repository, creating a
  skeleton project of the required shape based on template files defined [here](<https://github.com/ministryofjustice/hmpps-probation-integration-services/tree/main/templates/projects>)
- The workflow will submit a pull request to the [hmpps-delius-core-terraform](<https://github.com/ministryofjustice/hmpps-delius-core-terraform>)
  repository, creating the ECS infrastructure for the project
- Where possible workflow will configure any external dependencies, generate
  secrets and these into the GitHub environment for the project

## Consequences

Using a GitHub Actions workflow to bootstrap our integration projects means
that all code related to creating the services is contained in a single
repository. The workflow uses template files that are located close to the
projects using these as their initial base and the steps to bootstrap a new
project are defined in the .github/workflows/bootstrap workflow definition.

The workflow currently covers the following activity:

- Authentication setup
- Creating database secrets
- Creating an initial project structure and files
- Creating a Terraform definition for the ECS service
- Sentry configuration

As the number of projects we manage increases we intend to maintain
consistency in the organisation of the integration services. Creating each new
project from a small set of defined project options supports this as well as
providing ongoing feedback as to whether the defined templates are sufficient
to cover our activities or that a new template definition is needed.

Anything that is currently manual can be documented, the instructions made
clear and displayed as part of the workflow execution. Automating these manual
steps is a future goal for the Probation Integration team.

Numerous pre-built components are available for GitHub Actions that can be
used to execute individual processes in the workflow. This reduces the need to
create custom scripts for commonly used steps, such as rendering templates. It
is also possible to fall back to custom code where necessary.
