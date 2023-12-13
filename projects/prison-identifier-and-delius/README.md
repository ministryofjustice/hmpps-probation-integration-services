# Prison Identifier and Delius

## Business Need

HMPPS has a number of systems each holding information about a person's interaction with different aspects of the justice system. These systems have been set up and managed over a long period of time resulting in different identifiers being used for a person in each. In order to integrate these independent systems we must match the person records using the information held in each. To achieve this for NOMIS (NOMS Number) and Delius (CRN) we query the two systems and determine that likely matches based on the data. Once we have a likely match we can link the records by adding the NOMS Number to the person record in the Delius database. When this link is in place the two systems can be integrated in other ways to support case management as the person moves through different stages of HMPPS supervision.

## Context Map

![Context Map](./tech-docs/source/img/prison-identifier-and-delius-context-map.svg)

## Workflow Triggers

### Batch Processing

The matching process can be triggered by a request to the integration service API endpoint

| Business Event        | API Endpoint                 |
|-----------------------|------------------------------|
| List of CRNs to Match | /person/populate-noms-number |

### Domain Event Processing (Not Yet Implemented)

The matching process will be triggered by domain events raised by Delius, once these events are implemented

| Business Event                     | Message Event Type / Filter     |
|------------------------------------|---------------------------------|
| New Sentence Added to Delius       | probation-case.sentence.created |
| Sentence Changed in Delius         | probation-case.sentence.amended |
| Sentence Moved to New Delius Event | probation-case.sentence.move    |

## Workflows

### Batch Processing

![Batch Processing Workflow](./tech-docs/source/img/prison-identifier-and-delius-workflow-id-update.svg)

## Interfaces

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint | Required Role                                   |
|--------------|-------------------------------------------------|
| All          | ROLE\_PROBATION\_API_\_PRISON_IDENTIFIER__UPDATE |

## Concepts

### Person Record Matching Process

The logic used to match NOMIS person records to Delius person records uses the basic personal details, any additional identifiers and the recorded sentence dates.

![Matching Process](./tech-docs/source/img/prison-identifier-and-delius-match-process.svg)
