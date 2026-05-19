package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementActionsRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getEnforcementActionByCode
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getContactType
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.staff.getStaffByCode
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.TeamRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.team.getTeam
import uk.gov.justice.digital.hmpps.service.ContactLogService.Companion.REVIEW_ENFORCEMENT_STATUS
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class ContactEnforcementService(
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val enforcementRepository: EnforcementRepository,
    private val enforcementActionsRepository: EnforcementActionsRepository,
    private val eventRepository: EventRepository,
    private val requirementRepository: RequirementRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
) {
    fun updateEnforcementActionForContact(contact: Contact, enforcementActionCode: String) {
        val person = contact.person
        val staff = staffRepository.getStaffByCode(contact.staff?.code!!)
        val team = teamRepository.getTeam(contact.team?.code!!)
        val event = contact.event?.id?.let { eventRepository.findById(it).orElse(null) }
        val requirement = contact.requirement?.id?.let { requirementRepository.findById(it).orElse(null) }
        val enforcementAction = enforcementActionsRepository.getEnforcementActionByCode(enforcementActionCode)
        enforcementRepository.save(
            Enforcement(
                contact = contact,
                action = enforcementAction,
                responseDate = enforcementAction.responseByPeriod.let {
                    contact.startTime?.plusDays(it ?: 0)
                }
            )
        )
        contactRepository.save(
            Contact(
                person = person,
                event = event,
                requirement = requirement,
                type = enforcementAction.contactType,
                date = LocalDate.now(),
                startTime = ZonedDateTime.now(EuropeLondon),
                staff = staff,
                team = team,
                provider = team.provider,
                linkedContactId = contact.id,
                notes = """
                            ${DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now())}
                            Enforcement Action: ${enforcementAction.description}
                        """.trimIndent()
            )
        )
        event?.run {
            ftcCount = ftcCount + 1
            eventRepository.save(this)
            val ftcLimit = disposal?.type?.ftcLimit ?: return@run
            if (ftcCount > ftcLimit && !contactRepository.enforcementReviewExists(
                    id,
                    breachEnd,
                    REVIEW_ENFORCEMENT_STATUS
                )
            ) {
                val reviewType = contactTypeRepository.getContactType(REVIEW_ENFORCEMENT_STATUS)
                contactRepository.save(
                    Contact(
                        linkedContactId = contact.id,
                        type = reviewType,
                        date = LocalDate.now(),
                        startTime = ZonedDateTime.now(EuropeLondon),
                        person = person,
                        event = this,
                        staff = staff,
                        team = team,
                        provider = team.provider,
                        notes = null,
                    )
                )
            }
        }
    }
}