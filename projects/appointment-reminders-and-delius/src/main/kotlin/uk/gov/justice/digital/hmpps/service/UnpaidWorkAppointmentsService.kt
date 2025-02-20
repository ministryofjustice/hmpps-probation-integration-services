package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.logging.Logger.logger
import uk.gov.justice.digital.hmpps.properties.UpwAppointmentRemindersJobProperties
import uk.gov.justice.digital.hmpps.repository.UpwAppointmentRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.service.notify.NotificationClient
import java.time.LocalDate

@Service
class UnpaidWorkAppointmentsService(
    private val properties: UpwAppointmentRemindersJobProperties,
    private val upwAppointmentRepository: UpwAppointmentRepository,
    private val notificationClient: NotificationClient,
    private val telemetryService: TelemetryService,
) {
    fun sendUnpaidWorkAppointmentReminders(providerCode: String, templateIds: List<String>, daysInAdvance: Long) {
        upwAppointmentRepository.getUnpaidWorkAppointments(LocalDate.now().plusDays(daysInAdvance), providerCode)
            .forEach {
                val telemetryProperties = mapOf(
                    "crn" to it.crn,
                    "providerCode" to providerCode,
                    "templateIds" to templateIds.joinToString(),
                    "upwAppointmentIds" to it.upwAppointmentIds,
                )
                if (it.crn !in properties.excludedCrns) {
                    val responses = templateIds.map { templateId ->
                        log.info("Sending SMS template $templateId to ${it.mobileNumber}")
                        val templateValues = mapOf("FirstName" to it.firstName, "NextWorkSession" to it.appointmentDate)
                        notificationClient.sendSms(templateId, it.mobileNumber, templateValues, it.crn)
                    }
                    telemetryService.trackEvent(
                        "UnpaidWorkAppointmentReminderSent",
                        telemetryProperties + mapOf("notificationIds" to responses.joinToString { response -> response?.notificationId.toString() })
                    )
                } else telemetryService.trackEvent("UnpaidWorkAppointmentReminderNotSent", telemetryProperties)
            }
    }

    companion object {
        private val log = logger()
    }
}