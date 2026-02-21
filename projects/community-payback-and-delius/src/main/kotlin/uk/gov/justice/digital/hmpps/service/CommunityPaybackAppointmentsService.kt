package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.model.CreateAppointment
import uk.gov.justice.digital.hmpps.appointments.model.ReferencedEntities
import uk.gov.justice.digital.hmpps.appointments.service.AppointmentService
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.entity.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.contact.toCodeDescription
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocationRepository
import uk.gov.justice.digital.hmpps.entity.staff.StaffRepository
import uk.gov.justice.digital.hmpps.entity.staff.toSupervisor
import uk.gov.justice.digital.hmpps.entity.unpaidwork.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class CommunityPaybackAppointmentsService(
    private val unpaidWorkProjectRepository: UnpaidWorkProjectRepository,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
    private val createUnpaidWorkAppointmentRepository: CreateUnpaidWorkAppointmentRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val staffRepository: StaffRepository,
    private val userAccessService: UserAccessService,
    private val appointmentService: AppointmentService,
    private val eventRepository: EventRepository,
    private val upwDetailsRepository: UpwDetailsRepository,
    private val officeLocationRepository: OfficeLocationRepository,
) {
    fun getAppointment(projectCode: String, appointmentId: Long, username: String): AppointmentResponse {
        val project = unpaidWorkProjectRepository.getByCode(projectCode)
        val appointment = unpaidWorkAppointmentRepository.getAppointment(appointmentId)
        val limitedAccess = userAccessService.caseAccessFor(username, appointment.person.crn)
        val case = appointment.toAppointmentResponseCase(limitedAccess)

        return AppointmentResponse(
            id = appointmentId,
            reference = appointment.contact.reference(),
            version = UUID(appointment.rowVersion, appointment.contact.rowVersion),
            project = Project(project),
            projectType = CodeName(
                project.projectType.description,
                project.projectType.code
            ),
            case = case,
            event = EventResponse(number = appointment.details.disposal.event.number.toInt()),
            supervisor = appointment.staff.toSupervisor(),
            team = CodeName(
                appointment.team.description,
                appointment.team.code
            ),
            provider = CodeName(
                appointment.team.provider.description,
                appointment.team.provider.code
            ),
            pickUpData = PickUp(
                location = appointment.pickUpLocation?.toPickUpLocation(),
                time = appointment.pickUpTime
            ),
            date = appointment.date,
            startTime = appointment.startTime,
            endTime = appointment.endTime,
            penaltyHours = penaltyTimeToHHmm(appointment.penaltyMinutes),
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

    fun getAppointments(
        crn: String?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        projectCodes: List<String>?,
        projectTypeCodes: List<String>?,
        outcomeCodes: List<String>?,
        pageable: Pageable
    ): Page<AppointmentsResponse> {
        val appointments = unpaidWorkAppointmentRepository.findAppointments(
            crn,
            fromDate,
            toDate,
            projectCodes,
            projectTypeCodes,
            outcomeCodes,
            pageable
        )
        return appointments.map {
            val outcome = it.outcomeId?.let { outcomeId ->
                referenceDataRepository.findById(outcomeId).map { ref ->
                    CodeDescription(ref.code, ref.description)
                }
            }
            val daysOverdue = if (outcome == null || it.date < LocalDate.now()) {
                ChronoUnit.DAYS.between(it.date, LocalDate.now())
            } else null

            AppointmentsResponse(
                id = it.id,
                date = it.date,
                startTime = it.startTime,
                endTime = it.endTime,
                daysOverdue = daysOverdue,
                case = AppointmentResponseCase(
                    crn = it.person.crn,
                    name = PersonName(
                        forename = it.person.forename,
                        surname = it.person.surname,
                        middleNames = listOfNotNull(it.person.secondName, it.person.thirdName)
                    ),
                    dateOfBirth = it.person.dateOfBirth,
                    currentExclusion = it.person.currentExclusion,
                    exclusionMessage = it.person.exclusionMessage,
                    currentRestriction = it.person.currentRestriction,
                    restrictionMessage = it.person.restrictionMessage,
                ),
                requirementProgress = RequirementProgress(
                    requiredMinutes = it.minutesOffered ?: 0,
                    completedMinutes = it.minutesCredited ?: 0,
                    adjustments = it.penaltyMinutes ?: 0
                ),
                outcome = outcome?.getOrNull()
            )
        }
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
            project = Project(project),
            appointmentSummaries = appointmentSummaries
        )
    }

    @Transactional
    fun createAppointments(
        projectCode: String,
        requests: CreateAppointmentsRequest
    ): List<CreatedAppointment> = with(requests.appointments) {
        val project = unpaidWorkProjectRepository.getByCode(projectCode).requireAvailabilityOnDates(map { it.date })
        val eventIds = eventRepository.getEventIds(map { it.crn to it.eventNumber })
        val upwDetails = upwDetailsRepository.getByEventIdIn(eventIds.values)
        val staff = staffRepository.getByCodeIn(mapNotNull { it.supervisor?.code })
        val locations = officeLocationRepository.getByCodeIn(mapNotNull { it.pickUp?.location?.code })
        val outcomes = contactOutcomeRepository.getByCodeIn(mapNotNull { it.outcome?.code })
        val workQuality = referenceDataRepository.findByDatasetCode(Dataset.UPW_WORK_QUALITY).associateBy { it.code }
        val behaviour = referenceDataRepository.findByDatasetCode(Dataset.UPW_BEHAVIOUR).associateBy { it.code }

        appointmentService
            .bulkCreate(map { request ->
                CreateAppointment(
                    reference = "$REFERENCE_PREFIX${request.reference}",
                    typeCode = ContactType.Code.UNPAID_WORK_APPOINTMENT.value,
                    relatedTo = ReferencedEntities(
                        crn = request.crn,
                        eventId = eventIds[request.crn to request.eventNumber]
                            ?: throw NotFoundException("Event ${request.eventNumber} not found for ${request.crn}")
                    ),
                    date = request.date,
                    startTime = request.startTime,
                    endTime = request.endTime,
                    staffCode = request.supervisor?.code ?: project.team.unallocatedStaff().code,
                    teamCode = project.team.code,
                    outcomeCode = request.outcome?.code,
                    notes = request.notes,
                    alert = request.alertActive,
                    sensitive = request.sensitive,
                    allowConflicts = true
                )
            })
            .associateBy { appointment -> single { "$REFERENCE_PREFIX${it.reference}" == appointment.reference } }
            .map { (request, appointment) ->
                CreateUnpaidWorkAppointment(
                    contactId = appointment.id,
                    personId = appointment.relatedTo.personId!!,
                    details = appointment.relatedTo.eventId!!.let { upwDetails[it].orNotFoundBy("Event ID", it) },
                    date = request.date,
                    startTime = request.startTime,
                    endTime = request.endTime,
                    pickUpLocation = request.pickUp?.location?.code
                        ?.let { code -> locations[code].orNotFoundBy("code", code) },
                    pickUpTime = request.pickUp?.time,
                    staff = request.supervisor?.code?.let { staff[it].orNotFoundBy("code", it) }
                        ?: project.team.unallocatedStaff(),
                    team = project.team,
                    project = project,
                    minutesOffered = request.minutesOffered
                        ?: ChronoUnit.MINUTES.between(request.startTime, request.endTime),
                    minutesCredited = request.minutesCredited,
                    penaltyMinutes = request.penaltyMinutes,
                    hiVisWorn = request.hiVisWorn,
                    workedIntensively = request.workedIntensively,
                    workQuality = request.workQuality?.let { workQuality[it.code].orNotFoundBy("code", it.code) },
                    behaviour = request.behaviour?.let { behaviour[it.code].orNotFoundBy("code", it.code) },
                    outcomeId = request.outcome?.let { outcomes[it.code].orNotFoundBy("code", it.code).id },
                    attended = appointment.attended,
                    complied = appointment.complied,
                    allocationId = request.allocationId,
                    notes = appointment.notes,
                    reference = request.reference
                )
            }
            .also(createUnpaidWorkAppointmentRepository::saveAll)
            .map { CreatedAppointment(id = it.id!!, reference = it.reference!!) }
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

        val outcome = request.outcome?.let { contactOutcomeRepository.getByCode(it.code) }
        unpaidWorkAppointment.apply {
            startTime = request.startTime
            endTime = request.endTime
            staff = staffRepository.getByCode(request.supervisor.code)
            hiVisWorn = request.hiVisWorn
            workedIntensively = request.workedIntensively
            minutesCredited = request.minutesCredited
            penaltyMinutes = request.penaltyMinutes
            workQuality = request.workQuality?.let { referenceDataRepository.getWorkQuality(it.code) }
            behaviour = request.behaviour?.let { referenceDataRepository.getBehaviour(it.code) }
            outcomeId = outcome?.id
            attended = outcome?.attended
            complied = outcome?.complied
            notes = appointment.notes
        }
    }

    private fun UnpaidWorkAppointment.toAppointmentResponseCase(
        limitedAccess: CaseAccess
    ) = AppointmentResponseCase(
        crn = person.crn,
        name = PersonName(
            forename = person.forename,
            surname = person.surname,
            middleNames = listOfNotNull(person.secondName, person.thirdName)
        ),
        dateOfBirth = person.dateOfBirth,
        currentExclusion = limitedAccess.userExcluded,
        exclusionMessage = limitedAccess.exclusionMessage,
        currentRestriction = limitedAccess.userRestricted,
        restrictionMessage = limitedAccess.restrictionMessage,
    )

    private fun penaltyTimeToHHmm(minutes: Long?): String {
        if (minutes == null || minutes == 0L) return "00:00"

        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }

    private fun Versioned.validateVersion(version: Long) {
        if (rowVersion != version) {
            throw ObjectOptimisticLockingFailureException(this::class.java, id!!)
        }
    }

    companion object {
        const val REFERENCE_PREFIX = "urn:uk:gov:hmpps:community-payback:appointment:"
    }
}