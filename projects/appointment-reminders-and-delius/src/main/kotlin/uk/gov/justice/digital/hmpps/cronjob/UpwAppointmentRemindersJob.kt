package uk.gov.justice.digital.hmpps.cronjob

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication.exit
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.service.UnpaidWorkAppointmentsService
import kotlin.system.exitProcess

@Component
@ConditionalOnProperty("jobs.unpaid-work-appointment-reminders.enabled")
class UpwAppointmentRemindersJob(
    private val service: UnpaidWorkAppointmentsService,
    private val applicationContext: ApplicationContext,
    @Value("\${jobs.unpaid-work-appointment-reminders.provider.code}") private val providerCode: String,
    @Value("\${jobs.unpaid-work-appointment-reminders.templates}") private val templateIds: List<String>,
) : ApplicationListener<ApplicationStartedEvent> {
    override fun onApplicationEvent(applicationStartedEvent: ApplicationStartedEvent) {
        service.sendUnpaidWorkAppointmentReminders(providerCode, templateIds)
        exitProcess(exit(applicationContext, { 0 }))
    }
}