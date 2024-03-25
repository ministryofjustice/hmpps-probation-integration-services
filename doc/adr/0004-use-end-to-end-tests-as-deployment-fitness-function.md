# 0004 - Use End-to-End Tests as a Fitness Function for Deployment

2022-10-17

## Status

Accepted

## Context

Each integration service we manage meets a specific need, usually to ensure
that activity in a digital service is reflected in one or more other digital
services in a correct and timely manner. As a highest layer of testing,
supplementing the usual unit and integration tests, we would like to include
end-to-end tests to determine whether an change to an integration is suitable
for deployment via our continuous delivery pipelines. We have previously
relied on isolated API testing as a fitness function but this only highlights
that the integration service we are deploying works as intended in specific
and constrained cases. API testing does not prove that the end-to-end
integration between two or more digital services continues to function
correctly after deploying changes. It also does not allow us to test the
behaviour of message consumers.

The convention in HMPPS Digital is to ensure that digital services maintain a
deployment environment named `dev` or `test` that is integrated with the
corresponding instances of other digital services, including HMPPS Auth, NOMIS
and NDelius. This means that we have an integrated environment within which to
run end-to-end tests. For many services the `dev`/`test` environment is only
accessible via an internal network route (e.g. via a VPN).

It is usually possible to write tests that exercise the user interfaces of the
HMPPS digital services we are supporting. These tests can simulate activity in
the service and ensure any integration points are triggered. If it is not
possible to trigger activity via a user interface then a service's API will
usually provide an adequate fallback. Once triggered, the same tests can then
exercise the user interface of the digital service at the other end of the
integration service to confirm the corresponding actions have been executed
correctly.

## Decision

- All integration services will include an end-to-end test that exercises each
  digital service involved as a user would, or a close as possible
- The end-to-end test will be included as a fitness function in our deployment
  pipeline
- Any failure of the end-to-end test will disallow the change from being
  deployed to the pre-prod and production environments

## Consequences

The team will take on responsibility for creating an end-to-end test for each
integration service we support, which includes scripting the digital services
that we are integrating with. The tests will use a test automation framework
and will be run in the HMPPS `dev`/`test` environment.

Changes to the user interfaces of the digital services we are working with may
break the tests. The team is aware that these tests may be brittle but the
maintenance overhead of this is deemed worth taking on as we gain the benefit
of ongoing confidence in our production deployments. In the future we would
like to work with other digital teams to jointly own the maintenance and
execution of the end-to-end tests, sharing both the overhead and the benefit.

As access to the digital services we need to exercise is restricted to the
internal HMPPS network we will use a self-hosted GitHub Actions runner to run
the tests. Using the self-hosted runner will enable us to include the internal
HMPPS `dev`/`test` environment in our CI pipeline and gate the deployment.
The integration service will need to be deployed to the `dev`/`test`
environment in order to run the integrated tests but a failure in that
environment would disallow the deployment to any of the higher environments.

The Probation Integration team will take on responsibility for setting up and
managing the self-hosted GitHub Actions runners that will be used.
