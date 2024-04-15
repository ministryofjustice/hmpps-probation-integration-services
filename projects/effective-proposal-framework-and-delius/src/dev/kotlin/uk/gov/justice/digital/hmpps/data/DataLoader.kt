package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository
import java.time.LocalDate

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
            PersonGenerator.DEFAULT_GENDER,
            PersonGenerator.DEFAULT,
            SentenceGenerator.DEFAULT_COURT,
            SentenceGenerator.DEFAULT_EVENT,
            SentenceGenerator.DEFAULT_SENTENCE,
            SentenceGenerator.DEFAULT_COURT_APPEARANCE,
            ProviderGenerator.DEFAULT,
            ManagerGenerator.DEFAULT_PERSON_MANAGER,
            ManagerGenerator.DEFAULT_RESPONSIBLE_OFFICER,
            PersonGenerator.EXCLUDED,
            PersonGenerator.RESTRICTED,
            SentenceGenerator.generateOgrsAssessment(LocalDate.now().minusDays(1), 3),
            SentenceGenerator.generateOgrsAssessment(LocalDate.now().minusDays(5), 1),
            SentenceGenerator.generateOgrsAssessment(LocalDate.now(), 5, softDeleted = true),
            PersonGenerator.RELEASED,
            ManagerGenerator.RELEASED_PERSON_MANAGER,
            SentenceGenerator.RELEASED_EVENT,
            SentenceGenerator.RELEASED_COURT_APPEARANCE,
            SentenceGenerator.RELEASED_SENTENCE,
            SentenceGenerator.RELEASED_CUSTODY,
            SentenceGenerator.RELEASE
        )
    }

    fun EntityManager.saveAll(vararg any: Any) = any.forEach { persist(it) }
}
