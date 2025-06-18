# Cron Job Monitor

A simple CronJob that checks for the status of other CronJobs and reports to Slack if any have failed or are running longer than expected.

To set timeouts for specific jobs, edit the `get_timeout_for_job` function in [container/scripts/notify.sh](container/scripts/notify.sh).