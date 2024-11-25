package uk.gov.justice.digital.hmpps.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.repository.UpwAppointmentRepository
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.service.notify.NotificationClient
import java.time.LocalDate

@Service
class UnpaidWorkAppointmentsService(
    private val upwAppointmentRepository: UpwAppointmentRepository,
    private val notificationClient: NotificationClient,
    private val telemetryService: TelemetryService,
    @Value("\${govuk-notify.templates.upw-appointment-reminder}") private val templateId: String,
) {
    fun sendUnpaidWorkAppointmentReminders(providerCode: String) {
        upwAppointmentRepository.getUnpaidWorkAppointments(LocalDate.now().plusDays(2), providerCode)
            .forEach {
                notificationClient.sendSms(
                    templateId,
                    it.mobileNumber,
                    mapOf("FirstName" to it.firstName, "NextWorkSession" to it.appointmentDate),
                    it.upwAppointmentIds
                )
                telemetryService.trackEvent(
                    "SentUnpaidWorkAppointmentReminder",
                    mapOf("crn" to it.crn, "upwAppointmentIds" to it.upwAppointmentIds)
                )
            }
    }
}