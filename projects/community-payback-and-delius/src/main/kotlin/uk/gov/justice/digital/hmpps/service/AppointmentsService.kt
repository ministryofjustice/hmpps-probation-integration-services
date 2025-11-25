package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.model.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class AppointmentsService(
    private val unpaidWorkProjectRepository: UnpaidWorkProjectRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val staffRepository: StaffRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val enforcementRepository: EnforcementRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val contactRepository: ContactRepository,
    private val userAccessService: UserAccessService
) {
    fun getAppointment(projectCode: String, appointmentId: Long, username: String): AppointmentResponse {
        val project = unpaidWorkProjectRepository.getUpwProjectByCode(projectCode)
        val appointment = unpaidWorkAppointmentRepository.getAppointment(appointmentId)
        val limitedAccess = userAccessService.caseAccessFor(username, appointment.person.crn)
        val case = appointment.toAppointmentResponseCase(limitedAccess)

        return AppointmentResponse(
            id = appointmentId,
            version = UUID(appointment.rowVersion, appointment.contact.rowVersion),
            project = AppointmentResponseProject(
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
            date = appointment.date,
            startTime = appointment.startTime,
            endTime = appointment.endTime,
            penaltyHours = penaltyTimeToHHmm(appointment.penaltyTime),
            outcome = appointment.contact.contactOutcome?.toCodeDescription(),
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

    fun getSession(
        projectCode: String,
        date: LocalDate,
        username: String
    ): SessionResponse {
        val project = unpaidWorkProjectRepository.getUpwProjectByCode(projectCode)
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
                outcome = it.contact.contactOutcome?.toCodeDescription(),
                requirementProgress = checkNotNull(minutes[it.details.id])
            )
        }

        return SessionResponse(
            project = SessionResponseProject(
                name = project.name,
                code = project.code,
                location = project.placementAddress?.toAppointmentResponseAddress()
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
        val appointment = unpaidWorkAppointmentRepository.getAppointment(appointmentId)

        val contact = appointment.contact

        require(request.endTime > request.startTime) {
            "End Time must be after Start Time"
        }

        require(
            request.outcome != null ||
                LocalDateTime.of(appointment.date, request.endTime) > LocalDateTime.now()
        ) {
            "Appointments in the past require an outcome"
        }

        val outcome = request.outcome?.let {
            contactOutcomeRepository.getContactOutcome(it.code)
        }

        val workQuality = request.workQuality?.let {
            referenceDataRepository.getWorkQuality(request.workQuality.code)
        }

        val behaviour = request.behaviour?.let {
            referenceDataRepository.getBehaviour(request.behaviour.code)
        }

        val staff = staffRepository.getStaff(request.supervisor.code)


        contact.update(request, outcome, staff)

        appointment.update(
            request, workQuality, behaviour,
            outcome, staff
        )

        if (request.alertActive == true) {
            val personManager =
                personManagerRepository.getActiveManagerForPerson(appointment.person.id!!)

            contactAlertRepository.save(
                ContactAlert(
                    contactId = contact.id,
                    contactTypeId = contact.contactTypeId,
                    personId = appointment.person.id,
                    personManagerId = personManager.id,
                    staffId = personManager.staff.id,
                    teamId = personManager.team.id
                )
            )
        }

        if (outcome?.complied == false) {
            val enforcementAction =
                enforcementActionRepository.getEnforcementAction(EnforcementAction.REFER_TO_PERSON_MANAGER)

            enforcementRepository.save(
                Enforcement(
                    contact = contact,
                    enforcementAction = enforcementAction,
                    responseDate = enforcementAction.responseByPeriod?.let { LocalDate.now().plusDays(it) }
                )
            )

            contactRepository.save(
                Contact(
                    linkedContactId = contact.id,
                    contactTypeId = enforcementAction.contactTypeId,
                    date = LocalDate.now(),
                    startTime = LocalTime.now(),
                    personId = contact.personId,
                    eventId = contact.eventId,
                    requirementId = contact.requirementId,
                    licenceConditionId = contact.licenceConditionId,
                    provider = contact.provider,
                    team = contact.team,
                    staff = contact.staff,
                    officeLocation = contact.officeLocation,
                    notes = """
                        |${contact.notes}
                        |
                        |${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}
                        |Enforcement Action: ${enforcementAction.description}
                    """.trimMargin(),
                )
            )
        }
    }

    private fun penaltyTimeToHHmm(minutes: Long?): String {
        if (minutes == null || minutes == 0L) return "00:00"

        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }

    private fun UpwAppointment.update(
        request: AppointmentOutcomeRequest, workQuality: ReferenceData?,
        behaviour: ReferenceData?, contactOutcome: ContactOutcome?, staff: Staff
    ) = apply {
        startTime = request.startTime
        endTime = request.endTime
        this.staff = staff
        hiVisWorn = request.hiVisWorn
        workedIntensively = request.workedIntensively
        minutesCredited = Duration.between(startTime, endTime).toMinutes()
        penaltyTime = request.penaltyMinutes
        this.workQuality = workQuality
        this.behaviour = behaviour
        contactOutcomeTypeId = contactOutcome?.id
        attended = contactOutcome?.attended
        complied = contactOutcome?.complied
        rowVersion = request.version.mostSignificantBits
    }

    private fun Contact.update(request: AppointmentOutcomeRequest, contactOutcome: ContactOutcome?, staff: Staff) =
        apply {
            startTime = request.startTime
            endTime = request.endTime
            this.staff = staff
            this.contactOutcome = contactOutcome
            notes = listOfNotNull(notes, request.notes).joinToString("\n\n")
            sensitive = request.sensitive
            alertActive = request.alertActive
            rowVersion = request.version.leastSignificantBits
        }
}