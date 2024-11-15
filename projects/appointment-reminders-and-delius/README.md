# Appointment Reminders and Delius

## Business Need

- Sending appointment reminders to people on probation to help improve compliance with supervision activities
- Providing a method of automating the sending of SMS messages via GOV.UK Notify
- Standardising the process of sending SMS appointment reminders across regions

## Workflows

### Sending SMS Reminders Based on Delius Data

The service will send appointment reminders to people on probation by combining the data in _Delius_ and the [GOV.UK Notify](https://www.notifications.service.gov.uk/) service. Data is gathered from Delius, validated and templated SMS messages are sent using the GOV.UK Notify API.

![Workflow Map](./tech-docs/source/img/east-of-england-upw-reminders.svg)

### Downloading CSV Data

A CSV file of the appointment reminders that the service will send on a particular day can be accessed via an API endpoint

## Interfaces

### Scheduled Job

- SMS messages are send via a scheduled job
- Telemetry is send to Application Insights for each SMS message sent

### API Access Control

API endpoints are secured by roles supplied by the HMPPS Auth client used in
the requests

| API Endpoint          | Required Role                                    |
|-----------------------|--------------------------------------------------|
| /upw-appointments.csv | PROBATION\_API\_\_REMINDERS\_\_UPW\_APPOINTMENTS |
