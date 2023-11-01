# Approved Premises and OASys

## Business Need

Read-only integration service meditating access to the OASys ORDS endpoints that provide a wide range of OASys data for the Approved Premises (CAS1) service. The integration service is a thin proxy over the OASys endpoints, providing access to the ORDS APIs using the OASys OAuth credentials and enabling per-endpoint access control via HMPPS Auth authorities. Accessing OASys APIs via the integration services also ensures that we include the OASys API calls in the Digital Studio observability tools such as Application Insights and Sentry.

## Context Map

![Context Map](./tech-docs/source/img/approved-premises-and-oasys-context-map.svg)

## Interfaces

### API Access Control

API endpoints are secured by authorities present in the HMPPS Auth client supplied with
requests

| API Endpoint | Required Role                         |
|--------------|---------------------------------------|
| All          | ROLE\_APPROVED\_PREMISES\_ASSESSMENTS |

## Concepts

### OASys ORDS Endpoints

OASys provides access to data via a set of API endpoints built using a technology called Oracle REST Data Services (ORDS). The ORDS APIs may add extra constraints to the set of data returned by OASys depending on how they have been set up.

### Assessment Response Constraints

The Approved Premises ORDS endpoints return assessment matching the following parameters:

|                     |                                      |
|---------------------|--------------------------------------|
| **Assessment Type** | OASys Layer 3 Assessments            |
| **Completed Date**  | Completed within the last six months |
| **Status**          | COMPLETE or LOCKED_INCOMPLETE        |
| **Signed Status**   | Signed or Unsigned                   |

Assessments that fall outside of these parameters (e.g. completed on a date older than six months ago) will not be returned by the ORDS endpoints and therefore are not available via the API
