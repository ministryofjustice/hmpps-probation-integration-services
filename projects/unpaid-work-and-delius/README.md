# Unpaid Work and Delius

Service that supports integration between the [Unpaid Work Service](https://github.com/ministryofjustice/hmpps-risk-assessment-ui)
and [Delius](https://github.com/ministryofjustice/delius). The service
provides an API for gathering data on the person on probation and background
information on the probation case. The integration service also listens for
events indicating a new unpaid work document has been completed and is
responsible for updating the Delius case record with a copy of the resulting
unpaid work document.

## Business Need

Supporting the Unpaid Work Service with background and case information used
when placing people on suitable community payback schemes. Ensuring
minimal double-keying is required when working through the unpaid work process
and ensuring the probation case record in Delius is updated to reflect the
activity in the Unpaid Work Service.

## Context Maps

![Context Maps](./tech-docs/source/img/unpaid-work-and-delius-context-maps.png)
