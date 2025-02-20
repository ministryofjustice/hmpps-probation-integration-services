package uk.gov.justice.digital.hmpps.cronjob

import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.job.CronJobHelper.runThenExit
import uk.gov.justice.digital.hmpps.service.UnpaidWorkAppointmentsService

@Component
@ConditionalOnProperty("jobs.unpaid-work-appointment-reminders.enabled")
class UpwAppointmentRemindersJob(
    private val service: UnpaidWorkAppointmentsService,
    private val applicationContext: ApplicationContext,
    @Value("\${jobs.unpaid-work-appointment-reminders.provider.code}") private val providerCode: String,
    @Value("\${jobs.unpaid-work-appointment-reminders.templates}") private val templateIds: List<String>,
    @Value("\${jobs.unpaid-work-appointment-reminders.days-in-advance}") private val daysInAdvance: Long,
) : ApplicationListener<ApplicationStartedEvent> {
    @WithSpan("JOB unpaid-work-appointment-reminders")
    override fun onApplicationEvent(applicationStartedEvent: ApplicationStartedEvent) = applicationContext.runThenExit {
        service.sendUnpaidWorkAppointmentReminders(providerCode, templateIds, daysInAdvance)
    }
}