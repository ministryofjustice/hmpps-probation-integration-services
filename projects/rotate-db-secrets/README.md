# Cron Job Monitor

A CronJob that creates a new secret for a delius db project and restarts the project.

This creates a docker image that can run sqlplus commands and kubect commands

THIS IS A WORK IN PROGRESS - the cron is currently disabled (set to date that never exists)

RUN WITH CARE. 
Ensure that the prob_int_service_prof profile is set on all service users
This script performs the following for each probation integration service that has a database credential:

1. Gets the current username and password
2. Generates a new password
3. Makes a DB call to set the new password
4. Updates the kubernetes secret
5. Restarts the service