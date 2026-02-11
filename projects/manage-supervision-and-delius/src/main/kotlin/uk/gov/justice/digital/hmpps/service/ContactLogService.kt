package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContact
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContactResponse
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ContactTypeRequirementTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.getStaffByCode
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.getTeam
import uk.gov.justice.digital.hmpps.messaging.Notifier

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
    private val registrationRepository: RegistrationRepository,
    private val notifier: Notifier
) : AuditableService(auditedInteractionService) {

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

            val savedContact = contactRepository.save(
                Contact(
                    person = person,
                    type = contactType,
                    description = createContact.description,
                    event = event,
                    requirement = requirement,
                    staff = staff,
                    team = team,
                    provider = team.provider,
                    notes = """
                    ${createContact.notes}

                    This contact was automatically created by the Manage Supervision integrations service.
                """.trimIndent(),
                    alert = createContact.alert,
                    sensitive = createContact.sensitive,
                    isVisor = createContact.visorReport
                )
            )

            val category = resolveMappaCategory(person.id)

            notifier.contactCreated(savedContact.id, createContact.visorReport, category, crn)

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

    private fun resolveMappaCategory(offenderId: Long): Int {
        val registration = registrationRepository
            .findByPersonIdAndTypeCodeOrderByIdDesc(
                offenderId,
                "MAPP"
            )
            .firstOrNull()

        return when (registration?.category?.code) {
            "M1" -> 1
            "M2" -> 2
            "M3" -> 3
            "M4" -> 4
            else -> 0
        }
    }
}