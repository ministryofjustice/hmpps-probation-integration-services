package uk.gov.justice.digital.hmpps.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonCrn
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.sentence.component.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.component.Requirement
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.*
import java.time.ZonedDateTime

@Transactional
@Service
class AppointmentService(
    private val licenceConditionRepository: LicenceConditionRepository,
    private val requirementRepository: RequirementRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val officeLocationRepository: OfficeLocationRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val contactOutcomeRepository: ContactOutcomeRepository,
    private val contactRepository: ContactRepository,
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
        contactRepository.saveAll(request.appointments.map { it.asEntity() })
    }

    fun update(request: UpdateAppointmentsRequest) {
        val existing = request.appointments.chunked(500)
            .map { list ->
                contactRepository.findByExternalReferenceIn(list.map { "${Contact.REFERENCE_PREFIX}${it.reference}" }
                    .toSet())
            }.flatten()
        existing.forEach { c ->
            request.findByReference(c.externalReference!!)?.also { c.update(it) }
        }
    }

    fun delete(request: DeleteAppointmentsRequest) {
        request.appointments.map { "${Contact.REFERENCE_PREFIX}${it.reference}" }.toSet()
            .chunked(500)
            .forEach { contactRepository.softDeleteByExternalReferenceIn(it.toSet()) }
    }

    fun statusChanged(crn: String, occurredAt: ZonedDateTime, info: StatusInfo) {
        contactRepository.save(info.asEntity(crn, occurredAt))
    }

    private fun StatusInfo.asEntity(crn: String, occurredAt: ZonedDateTime): Contact {
        val (requirement, licenceCondition) = when (sourcedFromEntityType) {
            StatusInfo.EntityType.LICENCE_CONDITION -> getComponent(null, sourcedFromEntityId)
            StatusInfo.EntityType.REQUIREMENT -> getComponent(sourcedFromEntityId, null)
        }
        val event = requirement?.disposal?.event ?: licenceCondition?.disposal?.event
        requireNotNull(event) { "Appointment component not found" }
        check(crn == event.person.crn) { "CRN and component do not match" }
        val manager = requireNotNull(event.person.manager) { "Person manager not found" }
        return Contact(
            person = event.person.asPersonCrn(),
            event = event,
            requirement = requirement,
            licenceCondition = licenceCondition,
            date = occurredAt.toLocalDate(),
            startTime = occurredAt,
            notes = null,
            sensitive = false,
            provider = manager.team.provider,
            team = manager.team,
            staff = manager.staff,
            type = contactTypeRepository.getByCode(newStatus.contactTypeCode),
            externalReference = null
        )
    }

    private fun CreateAppointmentRequest.asEntity(): Contact {
        val (requirement, licenceCondition) = getComponent(requirementId, licenceConditionId)
        val event = requirement?.disposal?.event ?: licenceCondition?.disposal?.event
        requireNotNull(event) { "Appointment component not found for $reference" }
        val team = teamRepository.getByCode(team.code)
        return Contact(
            person = event.person.asPersonCrn(),
            event = event,
            requirement = requirement,
            licenceCondition = licenceCondition,
            date = date,
            startTime = ZonedDateTime.of(date, startTime, EuropeLondon),
            endTime = ZonedDateTime.of(date, endTime, EuropeLondon),
            notes = notes,
            sensitive = sensitive,
            provider = team.provider,
            team = team,
            staff = staffRepository.getByCode(staff.code),
            location = location?.code?.let { officeLocationRepository.getByCode(it) },
            type = contactTypeRepository.getByCode(ContactType.APPOINTMENT),
            externalReference = "${Contact.REFERENCE_PREFIX}$reference",
            outcome = outcome?.code?.let { contactOutcomeRepository.getByCode(it) },
        )
    }

    private fun getComponent(requirementId: Long?, licenceConditionId: Long?): Pair<Requirement?, LicenceCondition?> {
        return (requirementId?.let { requirementRepository.findByIdOrNull(it) } to
            licenceConditionId?.let { licenceConditionRepository.findByIdOrNull(it) })
    }

    private fun Contact.update(request: UpdateAppointmentRequest) = apply {
        val newTeam = if (request.team.code == team.code) team else teamRepository.getByCode(request.team.code)
        date = request.date
        startTime = ZonedDateTime.of(date, request.startTime, EuropeLondon)
        endTime = ZonedDateTime.of(date, request.endTime, EuropeLondon)
        provider = newTeam.provider
        team = newTeam
        staff = staffRepository.getByCode(request.staff.code)
        location = request.location?.code?.let { officeLocationRepository.getByCode(it) }
        sensitive = sensitive || request.sensitive
        outcome = request.outcome?.code?.let { contactOutcomeRepository.getByCode(it) }
        request.notes?.also { appendNotes(it) }
    }
}

fun Person.asPersonCrn() = PersonCrn(id, crn, softDeleted)

fun Contact.asAppointment() = AppointmentResponse(
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
