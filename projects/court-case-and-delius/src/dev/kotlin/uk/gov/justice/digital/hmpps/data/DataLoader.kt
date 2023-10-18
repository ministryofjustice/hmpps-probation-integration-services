package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.BusinessInteractionGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.CourtCaseNoteGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.courtappearance.entity.Outcome
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.ZonedDateTime

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val em: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        em.saveAll(
            BusinessInteractionGenerator.CASE_NOTES_MERGE,
            ContactTypeGenerator.CONTACT_TYPE,
            PersonGenerator.NEW_TO_PROBATION,
            PersonGenerator.CURRENTLY_MANAGED,
            PersonGenerator.PREVIOUSLY_MANAGED,
            PersonGenerator.NO_SENTENCE
        )

        em.saveAll(StaffGenerator.ALLOCATED, StaffGenerator.UNALLOCATED)

        em.saveAll(
            PersonGenerator.generatePersonManager(PersonGenerator.NEW_TO_PROBATION),
            PersonGenerator.generatePersonManager(PersonGenerator.CURRENTLY_MANAGED)
        )

        val noSentenceEvent = SentenceGenerator.generateEvent(PersonGenerator.NO_SENTENCE)
        val noSentenceManager = SentenceGenerator.generateOrderManager(noSentenceEvent, StaffGenerator.UNALLOCATED)
        val outcome = Outcome(Outcome.Code.AWAITING_PSR.value, IdGenerator.getAndIncrement())
        val courtAppearance = SentenceGenerator.generateCourtAppearance(noSentenceEvent, outcome)
        em.saveAll(noSentenceEvent, noSentenceManager, outcome, courtAppearance)

        val newEvent = SentenceGenerator.generateEvent(PersonGenerator.NEW_TO_PROBATION)
        val newSentence = SentenceGenerator.generateSentence(newEvent)
        val newManager = SentenceGenerator.generateOrderManager(newEvent, StaffGenerator.UNALLOCATED)
        em.saveAll(newEvent, newSentence, newManager)

        val currentEvent = SentenceGenerator.generateEvent(PersonGenerator.CURRENTLY_MANAGED, inBreach = true)
        val currentSentence = SentenceGenerator.generateSentence(currentEvent)
        val currentManager = SentenceGenerator.generateOrderManager(currentEvent, StaffGenerator.ALLOCATED)
        em.saveAll(currentEvent, currentSentence, currentManager)

        val preEvent = SentenceGenerator.generateEvent(PersonGenerator.PREVIOUSLY_MANAGED, active = false)
        val preSentence = SentenceGenerator.generateSentence(preEvent, ZonedDateTime.now().minusDays(7), active = false)
        val preManager = SentenceGenerator.generateOrderManager(preEvent, StaffGenerator.ALLOCATED)
        em.saveAll(preEvent, preSentence, preManager)

        em.merge(CourtCaseNoteGenerator.CASE_NOTE)
    }
}

fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
