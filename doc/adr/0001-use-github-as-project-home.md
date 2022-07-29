# 0001 - Use GitHub as the Project and Service Home

2022-07-29

## Status

Accepted

## Context

As the Probation Integration team takes on more responsibility for
services that integrate with Delius we are creating more software
overhead. The team would like to minimise the context switching required
when developing and managing this software, particularly the number of
SaaS services that are involved in daily work. For this reason the team
investigated ways of collecting the various services and workflows into
a single user interface

As a team we would like to ensure we are using technologies commonly
understood and widely used within both the MoJ and HMPPS. As our GitHub
organisation is used as the base for identity across the technical
services provided by the MoJ we believe it is a stable base for this
project and service user interface. GitHub also provides integration
points for some of the services we use and where these don't exist it is
possible to create custom integration code to make services available in
the UI

## Decision

- Use the GitHub UI as the development and service 'home'
- Ensure as much as possible is managed via the GitHub repository settings or workflow code
- Use pre-integrated technical services where possible
- Link to non-integrated services from the README where necessary (e.g. Sentry)

## Consequences

The GitHub UI is used as the home for the project and service. This
enables us to collect a number of technical service management features
under one user interface and greatly reduces the need log in to multiple
services when managing both the development and operation of the
service. The list of elements currently under the GitHub UI are:

- Code repository and development workflow (GitHub code repository, search, pull request etc.)
- Continuous integration (GitHub Actions main and branch workflows)
- Deployment pipeline (GitHub Actions deployment workflow and UI)
- Code analysis (GitHub Actions SonarCloud workflow and UI)
- Dependency scanning and automatic upgrades (Dependabot automatic PRs)
- Security scanning (GitHub Actions security workflow and alerts UI)
- Secrets management (GitHub Actions secrets management UI)
- End-to-End testing (GitHub Actions self-hosted runner and HTML report)
- Environment management (Deployment history UI)

Making this decision does take us away from the common HMPPS tooling of
CircleCI for pipelines and secrets management and also means that
contributing to the shared templates and scripts used across HMPPS is
more difficult. It is, however, the team's intention to identify
elements that may be useful across the organisation and contribute to
the shared tooling. This may be slightly more effort due to the
differences in hosting environment and project shape
