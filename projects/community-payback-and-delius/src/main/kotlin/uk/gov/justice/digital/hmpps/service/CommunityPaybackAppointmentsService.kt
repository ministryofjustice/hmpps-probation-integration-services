package uk.gov.justice.digital.hmpps.service

import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.service.AppointmentService
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate
import java.util.*

@Service
class CommunityPaybackAppointmentsService(
    private val unpaidWorkProjectRepository: UnpaidWorkProjectRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val staffRepository: StaffRepository,
    private val userAccessService: UserAccessService,
    private val appointmentService: AppointmentService,
) {
    fun getAppointment(projectCode: String, appointmentId: Long, username: String): AppointmentResponse {
        val project = unpaidWorkProjectRepository.getByCode(projectCode)
        val appointment = unpaidWorkAppointmentRepository.getAppointment(appointmentId)
        val limitedAccess = userAccessService.caseAccessFor(username, appointment.person.crn)
        val case = appointment.toAppointmentResponseCase(limitedAccess)

        return AppointmentResponse(
            id = appointmentId,
            version = UUID(appointment.rowVersion, appointment.contact.rowVersion),
            project = Project(
                name = project.name,
                code = project.code,
                location = project.placementAddress?.toAppointmentResponseAddress(),
                hiVisRequired = project.hiVisRequired
            ),
            projectType = NameCode(
                project.projectType.description,
                project.projectType.code
            ),
            case = case,
            event = EventResponse(number = appointment.details.disposal.event.number.toInt()),
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
                location = appointment.pickUpLocation?.toAppointmentResponseAddress(),
                time = appointment.pickUpTime
            ),
            date = appointment.date,
            startTime = appointment.startTime,
            endTime = appointment.endTime,
            penaltyHours = penaltyTimeToHHmm(appointment.penaltyTime),
            outcome = appointment.contact.outcome?.toCodeDescription(),
            enforcementAction = appointment.contact.latestEnforcementAction?.let { enforcementAction ->
                AppointmentResponseEnforcementAction(
                    code = enforcementAction.code,
                    description = enforcementAction.description,
                    respondBy = enforcementAction.responseByPeriod?.let { appointment.date.plusDays(it) }
                )
            },
            hiVisWorn = appointment.hiVisWorn,
            workedIntensively = appointment.workedIntensively,
            workQuality = appointment.workQuality?.let { WorkQuality.of(it.code) },
            behaviour = appointment.behaviour?.let { Behaviour.of(it.code) },
            notes = appointment.contact.notes,
            updatedAt = appointment.lastUpdatedDatetime,
            sensitive = appointment.contact.sensitive,
            alertActive = appointment.contact.alertActive
        )
    }

    fun getSession(projectCode: String, date: LocalDate, username: String): SessionResponse {
        val project = unpaidWorkProjectRepository.getByCode(projectCode)
        val appointments =
            unpaidWorkAppointmentRepository.findByDateAndProjectCodeAndDetailsSoftDeletedFalse(date, project.code)
        val upwDetailsIds = appointments.map { it.details.id }.distinct()
        val minutes = unpaidWorkAppointmentRepository.getUpwRequiredAndCompletedMinutes(upwDetailsIds)
            .associateBy { it.id }.mapValues { (_, v) -> v.toModel() }

        val appointmentSummaries = appointments.map {
            val limitedAccess = userAccessService.caseAccessFor(username, it.person.crn)

            SessionResponseAppointmentSummary(
                id = it.id,
                case = it.toAppointmentResponseCase(limitedAccess),
                outcome = it.contact.outcome?.toCodeDescription(),
                requirementProgress = checkNotNull(minutes[it.details.id])
            )
        }

        return SessionResponse(
            project = Project(
                name = project.name,
                code = project.code,
                location = project.placementAddress?.toAppointmentResponseAddress(),
                hiVisRequired = project.hiVisRequired,
            ),
            appointmentSummaries = appointmentSummaries
        )
    }

    @Transactional
    fun updateAppointmentOutcome(
        projectCode: String,
        appointmentId: Long,
        request: AppointmentOutcomeRequest
    ) {
        val unpaidWorkAppointment = unpaidWorkAppointmentRepository.getAppointment(appointmentId)
        unpaidWorkAppointment.validateVersion(request.version.mostSignificantBits)
        unpaidWorkAppointment.contact.validateVersion(request.version.leastSignificantBits)

        require(projectCode == unpaidWorkAppointment.project.code) {
            "Appointment is not for the provided project"
        }

        val appointment = appointmentService.update(request) {
            id = { unpaidWorkAppointment.contact.id }
            amendDateTime = { copy(startTime = it.startTime, endTime = it.endTime, allowConflicts = true) }
            applyOutcome = { copy(outcomeCode = it.outcome?.code) }
            reassign = { copy(staffCode = it.supervisor.code) }
            flagAs = { copy(alert = it.alertActive, sensitive = it.sensitive) }
            appendNotes = { it.notes }
        }

        val outcome = request.outcome?.let { contactOutcomeRepository.getContactOutcome(it.code) }
        unpaidWorkAppointment.apply {
            startTime = request.startTime
            endTime = request.endTime
            staff = staffRepository.getStaff(request.supervisor.code)
            hiVisWorn = request.hiVisWorn
            workedIntensively = request.workedIntensively
            minutesCredited = request.minutesCredited
            penaltyTime = request.penaltyMinutes
            workQuality = request.workQuality?.let { referenceDataRepository.getWorkQuality(it.code) }
            behaviour = request.behaviour?.let { referenceDataRepository.getBehaviour(it.code) }
            contactOutcomeTypeId = outcome?.id
            attended = outcome?.attended
            complied = outcome?.complied
            notes = appointment.notes
        }
    }

    private fun penaltyTimeToHHmm(minutes: Long?): String {
        if (minutes == null || minutes == 0L) return "00:00"

        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }

    private fun Versioned.validateVersion(version: Long) {
        if (rowVersion != version) {
            throw ObjectOptimisticLockingFailureException(this::class.java, id)
        }
    }
}