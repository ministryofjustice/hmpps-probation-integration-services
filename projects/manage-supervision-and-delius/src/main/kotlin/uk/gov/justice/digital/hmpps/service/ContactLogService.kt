package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContact
import uk.gov.justice.digital.hmpps.api.model.contact.CreateContactResponse
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.getStaffById
import java.time.LocalDate

@Service
class ContactLogService(
    auditedInteractionService: AuditedInteractionService,
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val staffRepository: StaffRepository,
    private val eventRepository: EventRepository,
    private val requirementRepository: RequirementRepository
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun createContact(
        crn: String,
        createContact: CreateContact
    ): CreateContactResponse {
        return audit(BusinessInteractionCode.ADD_CONTACT, username = UserContext.get()?.username) { audit ->
            val person = personRepository.getPerson(crn)

            audit["offenderId"] = person.id

            val staff = staffRepository.getStaffById(createContact.staffId)
            val team = staff.teams.firstOrNull { it.endDate == null || it.endDate.isAfter(LocalDate.now()) }
                ?: throw NotFoundException("Team", "staffId", createContact.staffId)

            if (!CreateContact.Type.entries.any { it.code == createContact.contactType }) {
                throw NotFoundException("CreateContact", "contactType", createContact.contactType)
            }
            val contactType = contactTypeRepository.getContactType(createContact.contactType)

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

            return@audit CreateContactResponse(savedContact.id)
        }
    }
}