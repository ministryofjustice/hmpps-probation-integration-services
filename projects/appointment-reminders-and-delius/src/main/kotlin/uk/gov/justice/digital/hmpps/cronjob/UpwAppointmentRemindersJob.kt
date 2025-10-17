package uk.gov.justice.digital.hmpps.cronjob

import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.config.JobConfig
import uk.gov.justice.digital.hmpps.job.CronJobHelper.runThenExit
import uk.gov.justice.digital.hmpps.service.UnpaidWorkAppointmentsService

@Component
@ConditionalOnProperty("job.name", havingValue = "unpaid-work-appointment-reminders")
class UpwAppointmentRemindersJob(
    private val service: UnpaidWorkAppointmentsService,
    private val applicationContext: ApplicationContext,
    private val jobConfig: JobConfig,
) : ApplicationListener<ApplicationStartedEvent> {
    @WithSpan("JOB unpaid-work-appointment-reminders")
    override fun onApplicationEvent(applicationStartedEvent: ApplicationStartedEvent) = applicationContext.runThenExit {
        service.sendUnpaidWorkAppointmentReminders(jobConfig)
    }
}