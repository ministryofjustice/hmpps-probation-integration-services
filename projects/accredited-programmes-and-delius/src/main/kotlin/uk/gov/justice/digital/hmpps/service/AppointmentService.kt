package uk.gov.justice.digital.hmpps.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonCrn
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.contact.enforcement.Enforcement
import uk.gov.justice.digital.hmpps.entity.contact.enforcement.EnforcementAction
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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
    private val enforcementActionRepository: EnforcementActionRepository,
    private val enforcementRepository: EnforcementRepository,
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
        contactRepository.saveAll(request.appointments.asEntities())
    }

    fun update(request: UpdateAppointmentsRequest) {
        request.appointments.map { "${Contact.REFERENCE_PREFIX}${it.reference}" }.toSet()
            .chunked(500)
            .flatMap { chunk -> contactRepository.findByExternalReferenceIn(chunk) }
            .mapNotNull { contact -> request.findByReference(contact.externalReference!!)?.let { it to contact } }
            .forEach { (request, contact) -> contact.update(request) }
    }

    fun delete(request: DeleteAppointmentsRequest) {
        request.appointments.map { "${Contact.REFERENCE_PREFIX}${it.reference}" }.toSet()
            .chunked(500)
            .forEach { contactRepository.softDeleteByExternalReferenceIn(it.toSet()) }
    }

    fun statusChanged(messageId: UUID, crn: String, occurredAt: ZonedDateTime, info: StatusInfo) {
        contactRepository.save(info.asEntity(messageId, crn, occurredAt))
    }

    private fun StatusInfo.asEntity(messageId: UUID, crn: String, occurredAt: ZonedDateTime): Contact {
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
            notes = notes,
            sensitive = false,
            provider = manager.team.provider,
            team = manager.team,
            staff = manager.staff,
            type = contactTypeRepository.getByCode(newStatus.contactTypeCode),
            externalReference = "urn:uk:gov:hmpps:accredited-programmes-service:$messageId"
        )
    }

    private fun List<CreateAppointmentRequest>.asEntities(): List<Contact> {
        val typeCodes = map { it.type.code }.distinct()
        val types = typeCodes.mapNotNull { contactTypeRepository.findByCode(it) }.associateBy { it.code }
        val outcomes = contactOutcomeRepository.getAllByCodeIn(mapNotNull { it.outcome?.code })
        val locations = officeLocationRepository.getAllByCodeIn(mapNotNull { it.location?.code })
        val teams = teamRepository.getAllByCodeIn(map { it.team.code })
        val staff = staffRepository.getAllByCodeIn(map { it.staff.code })
        val requirements = requirementRepository.getAllByCodeIn(mapNotNull { it.requirementId })
        val licenceConditions = licenceConditionRepository.getAllByCodeIn(mapNotNull { it.licenceConditionId })

        return map {
            val requirement = it.requirementId?.let { id -> requirements[id] }
            val licenceCondition = it.licenceConditionId?.let { id -> licenceConditions[id] }
            val event = checkNotNull(
                listOfNotNull(requirement?.disposal?.event, licenceCondition?.disposal?.event).firstOrNull()
            ) { "Appointment component not found" }
            val team = teams[it.team.code].orNotFoundBy("code", it.team.code)
            Contact(
                person = event.person.asPersonCrn(),
                event = event,
                requirement = requirement,
                licenceCondition = licenceCondition,
                date = it.date,
                startTime = ZonedDateTime.of(it.date, it.startTime, EuropeLondon),
                endTime = ZonedDateTime.of(it.date, it.endTime, EuropeLondon),
                notes = it.notes,
                sensitive = it.sensitive,
                provider = team.provider,
                team = team,
                staff = staff[it.staff.code].orNotFoundBy("code", it.staff.code),
                location = it.location?.code?.let { code -> locations[code].orNotFoundBy("code", code) },
                type = types[it.type.code].orNotFoundBy("code", it.type.code),
                externalReference = "${Contact.REFERENCE_PREFIX}${it.reference}",
                outcome = it.outcome?.code?.let { code -> outcomes[code].orNotFoundBy("code", code) },
            )
        }
    }

    private fun getComponent(requirementId: Long?, licenceConditionId: Long?) = Pair(
        requirementId?.let { requirementRepository.findByIdOrNull(it) },
        licenceConditionId?.let { licenceConditionRepository.findByIdOrNull(it) }
    )

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
        outcome = request.outcome?.code?.let { contactOutcomeRepository.getByCode(it) }?.also { outcome ->
            complied = outcome.complied
            attended = outcome.attended
            if (outcome.complied == false) {
                applyEnforcementAction(EnforcementAction.REFER_TO_PERSON_MANAGER)
                updateFailureToComplyCount()
            }
        }
        request.notes?.also { appendNotes(it) }
    }

    private fun Contact.applyEnforcementAction(actionCode: String) {
        val action: EnforcementAction = enforcementActionRepository.getByCode(actionCode)
        if (!enforcementRepository.existsByContactId(id)) {
            enforcementRepository.save(
                Enforcement(
                    contact = this,
                    action = action,
                    responseDate = action.responseByPeriod?.let { ZonedDateTime.now().plusDays(it) }
                )
            )
            contactRepository.save(
                Contact(
                    linkedContactId = id,
                    type = action.contactType,
                    date = LocalDate.now(),
                    startTime = ZonedDateTime.now(),
                    person = person,
                    event = event,
                    requirement = requirement,
                    licenceCondition = licenceCondition,
                    provider = provider,
                    team = team,
                    staff = staff,
                    location = location,
                    notes = """
                        |$notes
                        |
                        |${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}
                        |Enforcement Action: ${action.description}
                    """.trimMargin(),
                )
            )
        }
        enforcementActionId = action.id
        enforcement = true
    }

    private fun Contact.updateFailureToComplyCount() {
        if (event == null) return
        event.ftcCount = contactRepository.countFailureToComply(event)

        val ftcLimit = event.disposal?.type?.ftcLimit ?: return
        if (event.ftcCount > ftcLimit && !contactRepository.enforcementReviewExists(event.id, event.breachEnd)) {
            createEnforcementReviewContact()
        }
    }

    private fun Contact.createEnforcementReviewContact() {
        contactRepository.save(
            Contact(
                linkedContactId = id,
                type = contactTypeRepository.getByCode(ContactType.REVIEW_ENFORCEMENT_STATUS),
                date = LocalDate.now(),
                startTime = ZonedDateTime.now(),
                person = person,
                event = event,
                requirement = requirement,
                licenceCondition = licenceCondition,
                provider = provider,
                team = team,
                staff = staff,
                location = location,
            )
        )
    }

    private fun Person.asPersonCrn() = PersonCrn(id, crn, softDeleted)

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
}

inline fun <reified T> Map<String, T>.reportMissing(codes: Set<String>) = also {
    val missing = codes - keys
    require(missing.isEmpty()) { "Invalid ${T::class.simpleName} codes: $missing" }
}

inline fun <reified T> Map<Long, T>.reportMissingIds(ids: Set<Long>) = also {
    val missing = ids - keys
    require(missing.isEmpty()) { "Invalid ${T::class.simpleName} IDs: $missing" }
}
