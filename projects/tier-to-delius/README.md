# Tier to Delius

Provides Delius interactions to the [HMPPS Tier](https://github.com/ministryofjustice/hmpps-tier) service for the purposes of calculating a management tier for the case. The resulting management tier is broadcast via a domain event and subsequently written to the Delius database by a consumer provided by the integration service.

## Probation Business Need

The management tier calculation is an measure of complexity algorithm of the probation case. It is used in case allocation decisions and case load tracking. The calculation is made on various factors of the probation case and is updated as these factors change. The calculation algorithm is run in the [HMPPS Tier](https://github.com/ministryofjustice/hmpps-tier) service but many of the input factors are held in Delius. The integration service provides API access to these factors and also consumes the calculation result and updates Delius to give probation practitioners access to the current value.

## Interfaces

### Message Formats

The service responds to an HMPPS Domain Event indicating a tier calculation has been completed.
[Tier to Delius Queue](https://github.com/ministryofjustice/cloud-platform-environments/blob/main/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/tier-to-delius-queue.tf).
The events are raised by the [HMPPS Tier](https://github.com/ministryofjustice/hmpps-tier) service

Example [messages](./src/dev/resources/messages/) are in the development source tree.

Incoming messages are filtered on `eventType` by the [SQS queue policy](https://github.com/ministryofjustice/cloud-platform-environments/blob/7e5a3f2c8718a21a01e1053de239fc201a398bbd/namespaces/live.cloud-platform.service.justice.gov.uk/hmpps-probation-integration-services-prod/resources/tier-to-delius-queue.tf#L5-L7)

## Event Triggers

| Business Event                      | Message Class      | Message Event Type / Filter |
|-------------------------------------|--------------------|-----------------------------|
| Updated Management Tier Calculation | HMPPS Domain Event | tier.calculation.complete   |

## Authorisation

API endpoints are secured by roles supplied by the HMPPS Auth client used in the requests

| API Endpoint        | Required Role     |
|---------------------|-------------------|
| /tier-details/{crn} | ROLE_TIER_DETAILS |
