package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
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
    fun sendUnpaidWorkAppointmentReminders(providerCode: String, templateIds: List<String>) {
        upwAppointmentRepository.getUnpaidWorkAppointments(LocalDate.now().plusDays(2), providerCode)
            .forEach {
                if (it.crn !in properties.excludedCrns) {
                    val responses = templateIds.map { templateId ->
                        val templateValues = mapOf("FirstName" to it.firstName, "NextWorkSession" to it.appointmentDate)
                        notificationClient.sendSms(templateId, it.mobileNumber, templateValues, it.crn)
                    }
                    telemetryService.trackEvent(
                        "UnpaidWorkAppointmentReminderSent",
                        mapOf(
                            "crn" to it.crn,
                            "upwAppointmentIds" to it.upwAppointmentIds,
                            "templateIds" to templateIds.joinToString(),
                            "notificationIds" to responses.joinToString { response -> response?.notificationId.toString() }
                        )
                    )
                } else telemetryService.trackEvent(
                    "UnpaidWorkAppointmentReminderNotSent",
                    mapOf("crn" to it.crn, "upwAppointmentIds" to it.upwAppointmentIds)
                )
            }
    }
}