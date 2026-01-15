package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.model.Appointment
import uk.gov.justice.digital.hmpps.appointments.model.CreateAppointment
import uk.gov.justice.digital.hmpps.appointments.model.ReferencedEntities
import uk.gov.justice.digital.hmpps.appointments.model.UpdateAppointment.*
import uk.gov.justice.digital.hmpps.appointments.service.AppointmentService
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.datetime.toDeliusDate
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.component.SentenceComponent
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.*

@Transactional
@Service
class AccreditedProgrammesAppointmentService(
    private val licenceConditionRepository: LicenceConditionRepository,
    private val requirementRepository: RequirementRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactRepository: ContactRepository,
    private val appointmentService: AppointmentService,
) {
    fun getAppointments(request: GetAppointmentsRequest) = with(request) {
        require(toDate >= fromDate) { "toDate cannot be before fromDate" }
        require(requirementIds.isNotEmpty() || licenceConditionIds.isNotEmpty()) {
            "at least one requirementId or licenceConditionId must be provided"
        }

        GetAppointmentsResponse(
            contactRepository.findAllByComponentIdInDateRange(requirementIds, licenceConditionIds, fromDate, toDate)
                .map { it.asAppointment() }
                .sortedWith(compareBy({ it.crn }, { it.date }, { it.startTime?.toLocalTime() }))
                .groupBy { it.crn }
        )
    }

    fun create(request: CreateAppointmentsRequest) {
        with(request.appointments) {
            val requirements = requirementRepository.getAllByCodeIn(mapNotNull { it.requirementId })
            val licenceConditions = licenceConditionRepository.getAllByCodeIn(mapNotNull { it.licenceConditionId })

            val createdAppointments = appointmentService.create(map { request ->
                val component = requireNotNull(
                    requirements[request.requirementId] ?: licenceConditions[request.licenceConditionId]
                ) { "Either requirementId or licenceConditionId must be provided" }

                CreateAppointment(
                    reference = "${Contact.REFERENCE_PREFIX}${request.reference}",
                    typeCode = request.type.code,
                    relatedTo = ReferencedEntities(
                        personId = component.disposal.event.person.id,
                        eventId = component.disposal.event.id,
                        requirementId = request.requirementId,
                        licenceConditionId = request.licenceConditionId
                    ),
                    date = request.date,
                    startTime = request.startTime,
                    endTime = request.endTime,
                    outcomeCode = request.outcome?.code,
                    locationCode = request.location?.code,
                    staffCode = request.staff.code,
                    teamCode = request.team.code,
                    notes = request.notes,
                    sensitive = request.sensitive
                )
            })

            val teams = teamRepository.getAllByCodeIn(map { it.team.code })
            val staff = staffRepository.getAllByCodeIn(map { it.staff.code })
            val commencedContactType = contactTypeRepository.getByCode(ContactType.ORDER_COMPONENT_COMMENCED)
            val commencementContacts = createdAppointments.mapNotNull {
                if (it.typeCode == CreateAppointmentRequest.Type.PRE_GROUP_ONE_TO_ONE_MEETING.code) {
                    val component = requirements[it.relatedTo.requirementId]
                        ?: licenceConditions[it.relatedTo.licenceConditionId]
                    val team = teams[it.teamCode].orNotFoundBy("code", it.teamCode)
                    val staff = staff[it.staffCode].orNotFoundBy("code", it.staffCode)
                    component?.commenceComponent(it, team, staff, commencedContactType)
                } else null
            }
            contactRepository.saveAll(commencementContacts)
        }
    }

    fun update(request: UpdateAppointmentsRequest) {
        appointmentService.update(request.appointments) {
            reference = { "${Contact.REFERENCE_PREFIX}${it.reference}" }
            amendDateTime = { Schedule(it.date, it.startTime, it.endTime, allowConflicts = true) }
            reassign = { Assignee(it.staff.code, it.team.code, it.location?.code) }
            applyOutcome = { Outcome(it.outcome?.code) }
            appendNotes = { it.notes }
            flagAs = { Flags(sensitive = it.sensitive) }
        }
    }

    fun delete(request: DeleteAppointmentsRequest) {
        request.appointments.map { "${Contact.REFERENCE_PREFIX}${it.reference}" }.toSet()
            .chunked(500)
            .forEach { contactRepository.softDeleteByExternalReferenceIn(it.toSet()) }
    }

    private fun Contact.asAppointment() = AppointmentResponse(
        crn = person.crn,
        reference = externalReference?.takeLast(36),
        requirementId = requirement?.id,
        licenceConditionId = licenceCondition?.id,
        date = date,
        startTime = startTime,
        endTime = endTime,
        outcome = outcome?.let { AppointmentOutcome(it.code, it.description, it.attended, it.complied) },
        location = location?.let { CodedValue(it.code, it.description) },
        staff = staff.toProbationPractitioner { null },
        team = team.toCodedValue(),
        notes = notes,
        sensitive = sensitive
    )

    private fun SentenceComponent.commenceComponent(
        appointment: Appointment,
        team: Team,
        staff: Staff,
        contactType: ContactType
    ): Contact? {
        val shouldCreateCommencedContact = this.commencementDate == null
        this.commencementDate = appointment.date.atStartOfDay(EuropeLondon)
        this.notes = listOfNotNull(
            this.notes,
            "Actual Start Date set to ${appointment.date.toDeliusDate()} following notification from the Accredited Programmes – Intervention Service"
        ).joinToString(System.lineSeparator() + System.lineSeparator())

        return if (shouldCreateCommencedContact) {
            Contact(
                person = disposal.event.person.asPersonCrn(),
                event = disposal.event,
                requirement = this as? Requirement,
                licenceCondition = this as? LicenceCondition,
                date = appointment.date,
                provider = team.provider,
                team = team,
                staff = staff,
                type = contactType,
            )
        } else null
    }
}

inline fun <reified T> Map<String, T>.reportMissing(codes: Set<String>) = also {
    val missing = codes - keys
    require(missing.isEmpty()) { "Invalid ${T::class.simpleName} codes: $missing" }
}

inline fun <reified T> Map<Long, T>.reportMissingIds(ids: Set<Long>) = also {
    val missing = ids - keys
    require(missing.isEmpty()) { "Invalid ${T::class.simpleName} IDs: $missing" }
}
