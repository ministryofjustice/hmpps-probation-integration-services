# Approved Premises and Delius

Service that responds to Approved Premises domain events, calls the
[HMPPS Approved Premises API](https://github.com/ministryofjustice/hmpps-approved-premises-api)
and records referral progress against the probation case record in Delius. We
also provide an API to give probation case information to support making
approved premises referrals.

## Business Need

Supporting the Approved Premises service with the correct information required
to make an Approved Premises application and ensuring the Probation
Practitioner has an up-to-date view of activity in the Approved Premises
service when interacting with the probation case via Delius.

## Context Maps

![Context Maps](./tech-docs/source/img/approved-premises-and-delius-context-maps.png)
