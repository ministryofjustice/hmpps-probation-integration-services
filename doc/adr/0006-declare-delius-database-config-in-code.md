# 0006 - Declare Delius Database Integration Service Configuration in Code

## Status

Accepted

## Context

Delius integration services require a named database user to use when
connecting to and working with the Delius database. This database user must
either exist or be created in the Delius database in each deployment
environment before the service can be successfully deployed. The database user
must also be assigned permission to access specific database tables and
procedures before being able to manipulate data and run routines. As our
integration services are intended to be tightly scoped to a specific domain
and purpose it is possible for us to fully define and scope the database
access required for a specific service.

When manipulating data in the Delius database it is necessary to fully audit
any changes using the audit system of the Delius application. This system
requires a named Delius application user to use in the audit record. Each
service is assigned a specific named application user for this purpose and
this user must be created prior to service deployment in each environment.

If any of the Delius database configuration is missing in an environment the
integration service will fail to deploy.

## Decision

- We will create a declarative configuration file describing the database
  users, access and audit details in the source tree of each integration
  service
- This file will be located at `projects/{service-name}/deploy/database/access.yml`
- The file will be used as input to a database routine which will create and
  configure and correct users and permissions for the service to run
- The [routines for the database](<https://github.com/ministryofjustice/hmpps-delius-pipelines/tree/master/components/oracle/playbooks/probation_integration_access>)
  are deployed with the Delius infrastructure
- The database routine will be run as a step in the deployment pipeline for
  each environment

## Consequences

Defining and scoping the database access for each of our integration services
ensures that the range and impact of an integration on the Delius database is
clear. The access available to an individual service can be reviewed and
understood using the configuration in the codebase, rather than needing to
gather the information from other sources. It is also clear from the commit
logs who has enabled access and why this is needed.

Creating the database users and access permissions as part of the application
deployment pipeline means that there are no external manual steps required
when releasing an application into a new environment, making the deployment
process less prone to failure.

Accessing the database user credentials via the GitHub Actions workflow and
using these to configure the database access means that there is no need to
share the secret values manually across teams.
