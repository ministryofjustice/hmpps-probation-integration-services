package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.contact.*
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ContactTypeRequirementTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.getStaffByCode
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.getTeam
import uk.gov.justice.digital.hmpps.messaging.EventType
import uk.gov.justice.digital.hmpps.messaging.Notifier
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@Service
class ContactLogService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val staffRepository: StaffRepository,
    private val eventRepository: EventRepository,
    private val requirementRepository: RequirementRepository,
    private val contactAlertRepository: ContactAlertRepository,
    private val offenderManagerRepository: OffenderManagerRepository,
    private val teamRepository: TeamRepository,
    private val contactTypeRequirementTypeRepository: ContactTypeRequirementTypeRepository,
    private val notifier: Notifier,
    private val mappaCategoryResolverService: MappaCategoryResolverService,
    private val enforcementActionsRepository: EnforcementActionsRepository,
    private val contactEnforcementService: ContactEnforcementService,
    private val enforcementRepository: EnforcementRepository,
    private val telemetryService: TelemetryService,
) : AuditableService(auditedInteractionService) {
    companion object {
        const val REVIEW_ENFORCEMENT_STATUS = "ARWS"
    }

    @Transactional
    fun createContact(
        crn: String,
        createContact: CreateContact
    ): CreateContactResponse {
        return audit(BusinessInteractionCode.ADD_CONTACT, username = UserContext.get()?.username) { audit ->
            val person = personRepository.getPerson(crn)

            audit["offenderId"] = person.id

            val staff = staffRepository.getStaffByCode(createContact.staffCode)
            val team = teamRepository.getTeam(createContact.teamCode)

            if (!CreateContact.Type.entries.any { it.code == createContact.type }) {
                throw NotFoundException("CreateContact", "contactType", createContact.type)
            }
            val contactType = contactTypeRepository.getContactType(createContact.type)

            validateContactTypeLevel(contactType, createContact)

            val event = createContact.eventId?.let {
                eventRepository.findByIdAndActiveIsTrue(it)
            }
            val requirement = createContact.requirementId?.let {
                requirementRepository.getRequirement(it)
            }

            val contactOutcome = createContact.outcomeCode?.let { code ->
                contactTypeRepository.findSelectableOutcomesByTypeCode(contactType.code)
                    .firstOrNull { it.code == code }
                    .orNotFoundBy("code", code)
            }

            if (createContact.enforcementActionCode != null && contactOutcome == null) {
                throw InvalidRequestException("outcome is required when an enforcement action is provided")
            }

            val savedContact = contactRepository.save(
                Contact(
                    date = createContact.date,
                    startTime = createContact.date.atTime(createContact.time).atZone(EuropeLondon),
                    person = person,
                    type = contactType,
                    description = createContact.description,
                    outcome = contactOutcome,
                    event = event,
                    requirement = requirement,
                    staff = staff,
                    team = team,
                    provider = team.provider,
                    notes = """
                    ${createContact.notes}

                    This contact was created in the Manage people on probation service.
                """.trimIndent(),
                    alert = createContact.alert,
                    sensitive = createContact.sensitive,
                    isVisor = createContact.visorReport,
                    attended = contactOutcome?.outcomeAttendance,
                    complied = contactOutcome?.outcomeCompliantAcceptable
                )
            )

            if (createContact.enforcementActionCode != null) {
                val appliedAction = contactEnforcementService.updateEnforcementActionForContact(
                    savedContact,
                    createContact.enforcementActionCode
                )
                setEnforcementFlag(savedContact, appliedAction)
            } else {
                setEnforcementFlag(savedContact)
            }

            val category = mappaCategoryResolverService.resolveMappaCategory(person.id)

            notifier.contactCreated(savedContact.id, createContact.visorReport, category, crn, EventType.CREATED)

            if (createContact.alert) {
                val personManager = offenderManagerRepository.findOffenderManagersByPersonIdAndActiveIsTrue(person.id)
                    ?: throw NotFoundException(
                        "PersonManager",
                        "personId",
                        person.id
                    )
                contactAlertRepository.save(
                    ContactAlert(
                        contact = savedContact,
                        typeId = contactType.id,
                        personId = person.id,
                        personManagerId = personManager.id,
                        staff = personManager.staff,
                        teamId = personManager.team.id
                    )
                )
            }
            return@audit CreateContactResponse(savedContact.id)
        }
    }

    fun getContactTypes(): ContactTypesResponse = ContactTypesResponse(
        contactTypeRepository.findByCodeIn(CreateContact.Type.entries.map { it.code })
            .map { it.toContactTypeResponse() }
    )

    private fun validateContactTypeLevel(contactType: ContactType, createContact: CreateContact) {
        // If not an offender-level contact type, ensure eventId or requirementId is provided.
        if (!contactType.offenderContact && createContact.eventId == null && createContact.requirementId == null) {
            throw InvalidRequestException("Event ID or Requirement ID need to be provided for contact type ${contactType.code}")
        }

        // If offender-level only contact type, ensure no eventId or requirementId is provided.
        if ((contactType.offenderContact && !contactType.eventContact) && (createContact.eventId != null || createContact.requirementId != null)) {
            throw InvalidRequestException("Contact type ${contactType.code} cannot be associated with an event or requirement")
        }

        // If requirementId is provided, check the contactTypeRequirementType mapping to see if the contact type is valid for the requirement type
        if (createContact.requirementId != null) {
            val validRequirementTypes = contactTypeRequirementTypeRepository.findByIdContactTypeId(contactType.id)
                .map { it.id.requirementTypeId }
                .ifEmpty { return }
            val requirement = requirementRepository.getRequirement(createContact.requirementId)
                ?: throw NotFoundException("Requirement", "id", createContact.requirementId)

            val requirementType = requirement.mainCategory?.id
                ?: throw InvalidRequestException("Contact type ${contactType.code} is not valid for this requirement")

            if (requirementType !in validRequirementTypes) {
                throw InvalidRequestException("Contact type ${contactType.code} is not valid for requirement type ${requirement.mainCategory.code}")
            }
        }
    }

    fun ContactType.toContactTypeResponse() = ContactTypeResponse(
        code = code,
        description = description,
        isPersonLevelContact = offenderContact
    )

    @Transactional
    fun updateContact(
        contactId: Long,
        request: UpdateContact
    ) {
        val contact = contactRepository.getContact(contactId)
        CreateContact.Type.entries.find { it.code == contact.type.code }
            ?: throw InvalidRequestException("Contact type ${contact.type.code} is not valid for update")
        contact.date = request.dateTime.toLocalDate()
        contact.startTime = request.dateTime
        request.notes?.let { contact.appendNotes(it) }
        require(contact.sensitive != true || request.sensitiveFlag == true) { "Cannot un-flag a sensitive contact" }
        contact.sensitive = request.sensitiveFlag
        setEnforcementFlag(contact)
        contactRepository.save(contact)
    }

    fun getContactOutcomesForType(typeCode: String): ContactOutcomes = ContactOutcomes(
        contactTypeRepository.findSelectableOutcomesByTypeCode(typeCode)
            .map {
                val enforcementActions = enforcementActionsRepository
                    .findByContactOutcomeId(it.id)
                ContactOutcomeResponse(
                    code = it.code,
                    description = it.description,
                    enforcementActions = enforcementActions.map { ea ->
                        EnforcementActionResponse(
                            code = ea.code,
                            description = ea.description,
                            defaultResponsePeriodDays = ea.responseByPeriod
                        )
                    }
                )
            }
    )

    private fun setEnforcementFlag(
        contact: Contact,
        appliedAction: EnforcementAction? = contact.action,
    ) {
        if (contact.type.contactOutcomeFlag == true && contact.outcome == null) {
            contact.enforcementFlag = true
        }
        if (appliedAction != null && appliedAction.outstandingContactAction == true) {
            contact.enforcementFlag = true
        }
    }

    @Transactional
    fun updateContactOutcome(contactId: Long, request: UpdateContactOutcome) {
        val contact = contactRepository.getContact(contactId)
        val contactType = contact.type.code
        val contactOutcome = request.outcomeCode?.let { requestOutcomeCode ->
            contactTypeRepository.findSelectableOutcomesByTypeCode(contactType)
                .firstOrNull { it.code == requestOutcomeCode }
                .orNotFoundBy("code", requestOutcomeCode)
        }
        if (contact.complied == false && contactOutcome?.outcomeCompliantAcceptable == true) {
            telemetryService.trackEvent(
                "remove enforcement for a compliant contact",
                mapOf("crn" to contact.person.crn, "contactId" to contactId.toString())
            )
            contact.enforcement?.let { enforcementRepository.delete(it) }
            contact.enforcement = null
        }

        request.notes.let { contact.appendNotes(it) }


        if (request.alert == true && contact.alert != true) {
            val personManager =
                offenderManagerRepository.findOffenderManagersByPersonIdAndActiveIsTrue(contact.person.id)
                    ?: throw NotFoundException(
                        "PersonManager",
                        "personId",
                        contact.person.id
                    )
            contactAlertRepository.save(
                ContactAlert(
                    contact = contact,
                    typeId = contact.type.id,
                    personId = contact.person.id,
                    personManagerId = personManager.id,
                    staff = personManager.staff,
                    teamId = personManager.team.id
                )
            )
            contact.alert = true
        } else if (request.alert == false && contact.alert == true) {
            val existingAlerts = contactAlertRepository.findByContactId(contact.id)
            if (existingAlerts.isNotEmpty()) {
                contactAlertRepository.deleteByContactIdIn(listOf(contact.id))
            }
            contact.alert = false
        }

        require(contact.sensitive != true || request.sensitive == true) { "Cannot un-flag a sensitive contact" }
        contact.sensitive = request.sensitive
        contact.date = request.date
        contact.startTime = ZonedDateTime.ofLocal(request.date.atTime(request.time), EuropeLondon, null)
        contactOutcome?.let {
            contact.outcome = contactOutcome
            contact.attended = contactOutcome.outcomeAttendance
            contact.complied = contactOutcome.outcomeCompliantAcceptable
        }

        contactRepository.save(contact)
        if (request.enforcementActionCode != null) {
            val appliedAction =
                contactEnforcementService.updateEnforcementActionForContact(contact, request.enforcementActionCode)
            setEnforcementFlag(contact, appliedAction)
        } else {
            setEnforcementFlag(contact)
        }
    }
}
