package uk.gov.justice.digital.hmpps.service

import io.sentry.Sentry
import org.apache.commons.codec.digest.MurmurHash3
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.config.JobConfig
import uk.gov.justice.digital.hmpps.logging.Logger.logger
import uk.gov.justice.digital.hmpps.repository.UpwAppointmentRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.service.notify.NotificationClient
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.jvm.optionals.getOrNull

@Service
class UnpaidWorkAppointmentsService(
    private val upwAppointmentRepository: UpwAppointmentRepository,
    private val notificationClient: NotificationClient,
    private val telemetryService: TelemetryService,
    @Value("\${jobs.unpaid-work-appointment-reminders.excluded-crns:}") private val excludedCrns: List<String>,
    @Value("\${jobs.unpaid-work-appointment-reminders.excluded-project-codes:}") private val excludedProjectCodes: List<String>,
) {
    fun sendUnpaidWorkAppointmentReminders(config: JobConfig) {
        val alreadySentCrns = findCrnsForMessagesAlreadySent()

        upwAppointmentRepository.getUnpaidWorkAppointments(
            date = LocalDate.now().plusDays(config.daysInAdvance.toLong()),
            providerCode = config.provider.code,
            excludedProjectCodes = excludedProjectCodes,
            excludedCrns = excludedCrns + alreadySentCrns
        ).forEach {
            val telemetryProperties = mapOf(
                "crn" to it.crn,
                "providerCode" to config.provider.code,
                "upwAppointmentIds" to it.upwAppointmentIds,
            )
            val templates = config.getTemplatesFor(it.crn)
            val responses = templates.map { templateId ->
                log.info("Sending SMS template $templateId to ${it.mobileNumber}")
                val templateValues = mapOf(
                    "FirstName" to it.firstName,
                    "NextWorkSession" to it.appointmentDate
                )
                try {
                    notificationClient.sendSms(templateId, it.mobileNumber, templateValues, it.crn)
                } catch (e: Exception) {
                    telemetryService.trackEvent("UnpaidWorkAppointmentReminderFailure", telemetryProperties)
                    telemetryService.trackException(e, telemetryProperties)
                    Sentry.captureException(e)
                    return@forEach
                }
            }
            telemetryService.trackEvent(
                "UnpaidWorkAppointmentReminderSent",
                telemetryProperties + mapOf(
                    "templateIds" to templates.joinToString(),
                    "notificationIds" to responses.joinToString { response -> response?.notificationId.toString() }
                )
            )
        }
    }

    /**
     * Selects the appropriate templates to use for a given CRN, based on the configured trials.
     * A consistent hashing algorithm is used to ensure that the same CRN always gets the same template(s).
     */
    private fun JobConfig.getTemplatesFor(crn: String): List<String> {
        if (trials.isEmpty()) return templates
        val hash = MurmurHash3.hash32x86(crn.toByteArray())
        val buckets = listOf(templates) + trials.map { it.templates }
        val bucket = buckets[Integer.remainderUnsigned(hash, buckets.size)]
        return bucket.ifEmpty { templates } // If a trial is removed, fall back to default templates
    }

    private fun findCrnsForMessagesAlreadySent(): List<String> {
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)

        fun getPage(olderThan: String? = null) =
            notificationClient.getNotifications(null, "sms", null, olderThan).notifications
                .filter { it.sentAt.isPresent }

        val alreadySentNotifications = generateSequence(getPage()) { page ->
            page.minByOrNull { it.sentAt.get() }
                ?.takeIf { !it.sentAt.get().isBefore(today) }
                ?.let { oldestMessage -> getPage(oldestMessage.id.toString()) }
        }
            .flatten()
            .filter { !it.sentAt.get().isBefore(today) }

        return alreadySentNotifications.mapNotNull { it.reference.getOrNull() }.toList()
    }

    companion object {
        private val log = logger()
    }
}