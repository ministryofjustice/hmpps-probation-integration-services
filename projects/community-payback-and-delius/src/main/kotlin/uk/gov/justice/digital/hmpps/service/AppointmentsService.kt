package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
    private val contactRepository: ContactRepository
) {
    fun getAppointment(projectCode: String, appointmentId: Long, username: String): AppointmentResponse {
        val project = unpaidWorkProjectRepository.getUpwProjectByCode(projectCode)
        val appointment = unpaidWorkAppointmentRepository.getUpwAppointmentById(appointmentId)
            ?: throw NotFoundException("UPWAppointment", "appointmentId", appointmentId)

        return AppointmentResponse(
            id = appointmentId,
            version = UUID(appointment.rowVersion, appointment.contact.rowVersion),
            project = AppointmentResponseProject(
                name = project.name,
                code = project.code,
                location = project.placementAddress?.toAppointmentResponseAddress(),
                hiVisWorn = appointment.hiVisWorn
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
            workedIntensively = appointment.workedIntensively,
            workQuality = appointment.workQuality?.let { WorkQuality.valueOf(appointment.workQuality!!.code).value },
            behaviour = appointment.behaviour?.let { Behaviour.valueOf(appointment.behaviour!!.code).value },
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
            project = SessionResponseProject(
                name = project.name,
                code = project.code,
                location = project.placementAddress?.toAppointmentResponseAddress()
            ),
            appointmentSummaries = appointmentSummaries
        )
    }

    fun updateAppointmentOutcome(
        projectCode: String,
        appointmentId: Long,
        appointmentOutcome: AppointmentOutcomeRequest
    ) {
        val appointment = unpaidWorkAppointmentRepository.getUpwAppointmentById(appointmentId)
            ?: throw NotFoundException("UPWAppointment", "appointmentId", appointmentId)

        val contact = appointment.contact

        if (!appointmentOutcome.endTime.isAfter(appointmentOutcome.startTime)) {
            throw IllegalStateException("End Time must be after Start Time")
        }

        if (LocalDateTime.of(appointment.appointmentDate, appointmentOutcome.endTime).isBefore(LocalDateTime.now())
            && appointmentOutcome.outcome == null
        ) {
            throw IllegalStateException("Appointments in the past require an outcome")
        }

        val outcome = appointmentOutcome.outcome?.let {
            contactOutcomeRepository.findContactOutcomeByCode(it.code)
                ?: throw IllegalStateException("Contact outcome with code ${it.code} not found")
        }

        val workQualityCode = WorkQuality.of(appointmentOutcome.workQuality)?.name
        val behaviourCode = Behaviour.of(appointmentOutcome.behaviour)?.name

        val workQuality = workQualityCode?.let {
            referenceDataRepository.findByCodeAndDatasetCode(workQualityCode, Dataset.UPW_WORK_QUALITY)
                ?: throw IllegalStateException("Work quality reference data with code $workQualityCode not found")
        } ?: throw IllegalStateException("Work quality ${appointmentOutcome.workQuality} not recognised")

        val behaviour = behaviourCode?.let {
            referenceDataRepository.findByCodeAndDatasetCode(behaviourCode, Dataset.UPW_BEHAVIOUR)
                ?: throw IllegalStateException("Behaviour reference data with code $behaviourCode not found")
        } ?: throw IllegalStateException("Behaviour ${appointmentOutcome.behaviour} not recognised")

        val staff =
            appointmentOutcome.supervisor?.let {
                staffRepository.findStaffByCode(appointmentOutcome.supervisor.code)
                    ?: throw IllegalStateException("Staff with code ${appointmentOutcome.supervisor.code} not found")
            }

        unpaidWorkAppointmentRepository.save(
            appointment.update(
                appointmentOutcome, workQuality, behaviour,
                outcome, staff, contact.update(appointmentOutcome)
            )
        )

        if (appointmentOutcome.alertActive) {
            val personManager =
                personManagerRepository.findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(appointment.person.id!!)
                    ?: throw NotFoundException("PersonManager", "personId", appointment.person.id)

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

        if (outcome!!.complied == false) {
            val enforcementAction =
                enforcementActionRepository.findEnforcementActionByCode(EnforcementAction.REFER_TO_PERSON_MANAGER)
                    ?: throw IllegalStateException("No Enforcement Action with code ${EnforcementAction.REFER_TO_PERSON_MANAGER} found.")

            enforcementRepository.save(
                Enforcement(
                    contact = contact,
                    enforcementAction = enforcementAction,
                    responseDate = enforcementAction.responseByPeriod.let { LocalDate.now().plusDays(it) }
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
        request: AppointmentOutcomeRequest, workQuality: ReferenceData,
        behaviour: ReferenceData, contactOutcome: ContactOutcome?, staff: Staff?, contact: Contact
    ) = apply {
        startTime = request.startTime
        endTime = request.endTime
        staff?.let { this.staff = staff }
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

    private fun Contact.update(request: AppointmentOutcomeRequest) = apply {
        startTime = request.startTime
        endTime = request.endTime
        notes = notes?.let { it + System.lineSeparator() + System.lineSeparator() + request.notes }
        sensitive = request.sensitive
        alertActive = request.alertActive
        rowVersion = request.version.leastSignificantBits
    }
}