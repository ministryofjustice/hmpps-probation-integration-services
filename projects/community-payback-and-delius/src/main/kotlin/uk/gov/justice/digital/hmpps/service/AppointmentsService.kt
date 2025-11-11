package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@Service
class AppointmentsService(
    private val unpaidWorkProjectRepository: UnpaidWorkProjectRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository
) {
    fun getAppointment(projectCode: String, appointmentId: Long, username: String): AppointmentResponse {
        val project = unpaidWorkProjectRepository.getUpwProjectByCode(projectCode)
        val appointment = unpaidWorkAppointmentRepository.getUpwAppointmentById(appointmentId)

        return AppointmentResponse(
            id = appointmentId,
            version = UUID(appointment.rowVersion, appointment.contact.rowVersion),
            project = AppointmentResponseProject(
                name = project.name,
                code = project.code,
                location = project.placementAddress?.toAppointmentResponseAddress()
            ),
            projectType = NameCode(
                project.projectType.description,
                project.projectType.code
            ),
            case = appointment.toAppointmentResponseCase(),
            supervisor = AppointmentResponseSupervisor(
                code = appointment.staff.code,
                name = AppointmentResponseName(
                    forename = appointment.staff.forename,
                    surname = appointment.staff.surname,
                    middleNames = appointment.staff.middleName?.let { listOf(it) } ?: emptyList()
                )
            ),
            team = NameCode(
                appointment.team.description,
                appointment.team.code
            ),
            provider = NameCode(
                appointment.team.provider.description,
                appointment.team.provider.code
            ),
            pickUpData = AppointmentResponsePickupData(
                location = appointment.pickUpLocation.toAppointmentResponseAddress(),
                time = appointment.pickUpTime
            ),
            date = appointment.appointmentDate,
            startTime = appointment.startTime,
            endTime = appointment.endTime,
            penaltyHours = penaltyTimeToHHmm(appointment.penaltyTime),
            outcome = appointment.contact.contactOutcome?.toCodeDescription(),
            enforcementAction = appointment.contact.latestEnforcementAction?.let {
                AppointmentResponseEnforcementAction(
                    appointment.contact.latestEnforcementAction.code,
                    appointment.contact.latestEnforcementAction.description,
                    appointment.appointmentDate.plusDays(appointment.contact.latestEnforcementAction.responseByPeriod)
                )
            },
            hiVisWorn = appointment.hiVisWorn,
            workedIntensively = appointment.workedIntensively,
            workQuality = appointment.workQuality?.let { WorkQuality.valueOf(appointment.workQuality.code).value },
            behaviour = appointment.behaviour?.let { Behaviour.valueOf(appointment.behaviour.code).value },
            notes = appointment.contact.notes,
            updatedAt = appointment.lastUpdatedDatetime,
            sensitive = appointment.contact.sensitive,
            alertActive = appointment.contact.alertActive
        )
    }

    fun getSession(
        projectCode: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        username: String
    ): SessionResponse {
        val project = unpaidWorkProjectRepository.getUpwProjectByCode(projectCode)
        val appointments = unpaidWorkAppointmentRepository.getUpwAppointmentsByAppointmentDateAndStartTimeAndEndTime(
            date, startTime, endTime
        )

        val appointmentSummaries = appointments.map {
            val minutes = unpaidWorkAppointmentRepository.getUpwRequiredAndCompletedMinutes(it.upwDetailsId).toModel()

            SessionResponseAppointmentSummary(
                id = it.id,
                case = it.toAppointmentResponseCase(),
                outcome = it.contact.contactOutcome?.toCodeDescription(),
                requirementProgress = minutes,
            )
        }

        return SessionResponse(
            project = AppointmentResponseProject(
                name = project.name,
                code = project.code,
                location = project.placementAddress?.toAppointmentResponseAddress()
            ),
            appointmentSummaries = appointmentSummaries
        )
    }

    private fun penaltyTimeToHHmm(minutes: Long?): String {
        if (minutes == null || minutes == 0L) return "00:00"

        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }
}