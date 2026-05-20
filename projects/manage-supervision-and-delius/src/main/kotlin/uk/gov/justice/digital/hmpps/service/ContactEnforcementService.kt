package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementActionsRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getContactType
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
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
) {
    fun updateEnforcementActionForContact(contact: Contact, enforcementActionCode: String) {
        val contactOutcome = contact.outcome.orNotFoundBy("contactId", contact.id)
        val enforcementAction = requireNotNull(enforcementActionsRepository.findByContactOutcomeId(contactOutcome.id)
            .firstOrNull { it.code == enforcementActionCode }) { "Enforcement action not valid for outcome" }
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
                staff = contact.staff,
                team = contact.team,
                provider = contact.team?.provider,
                linkedContactId = contact.id,
                notes = null,
                licenceCondition = contact.licenceCondition,
                nsiId = contact.nsiId,
            )
        )
        contactRepository.save(contact.apply {
            this.appendNotes( """
                            ${DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now())}
                            Enforcement Action: ${enforcementAction.description}
                        """.trimIndent())
        })
        contact.event?.run {
            ftcCount = contactRepository.countFailureToComply(this)
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
                        staff = contact.staff,
                        team = contact.team,
                        provider = contact.team?.provider,
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