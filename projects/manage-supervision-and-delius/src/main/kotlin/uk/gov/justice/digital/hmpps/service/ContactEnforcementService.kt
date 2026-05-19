package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementActionsRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getContactType
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
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
        val staffCode = contact.staff?.code.orNotFoundBy("Contact", contact.id)
        val teamCode = contact.team?.code.orNotFoundBy("Contact", contact.id)
        val staff = staffRepository.getStaffByCode(staffCode)
        val team = teamRepository.getTeam(teamCode)
        val contactOutcome = contact.outcome.orNotFoundBy("contactId", contact.id)
        val enforcementAction = enforcementActionsRepository.findByContactOutcomeId(contactOutcome.id)
            .firstOrNull { it.code == enforcementActionCode }.orNotFoundBy(
                "EnforcementActionCode",
                enforcementActionCode
            )
        enforcementRepository.save(
            Enforcement(
                contact = contact,
                action = enforcementAction,
                responseDate = contact.startTime?.plusDays(enforcementAction.responseByPeriod ?: 0)
            )
        )
        contactRepository.save(
            Contact(
                person = contact.person,
                event = contact.event,
                requirement = contact.requirement,
                type = enforcementAction.contactType,
                date = LocalDate.now(),
                startTime = ZonedDateTime.now(EuropeLondon),
                staff = staff,
                team = team,
                provider = team.provider,
                linkedContactId = contact.id,
                notes = null,
                licenceCondition = contact.licenceCondition,
                nsiId = contact.nsiId,
            )
        )
        contactRepository.save(contact.apply {
            this.notes = """
                            ${DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now())}
                            Enforcement Action: ${enforcementAction.description}
                        """.trimIndent()
        })
        contact.event?.run {
            ftcCount = contactRepository.countFailureToComply(this).plus(1)
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
                        person = contact.person,
                        event = this,
                        staff = staff,
                        team = team,
                        provider = team.provider,
                        notes = null,
                        requirement = contact.requirement,
                        licenceCondition = contact.licenceCondition,
                        nsiId = contact.nsiId,
                    )
                )
            }
        }
    }
}