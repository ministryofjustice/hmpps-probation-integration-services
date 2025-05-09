package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessGenerator
import uk.gov.justice.digital.hmpps.data.generator.LimitedAccessUserGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager,
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        entityManager.persistAll(
            LimitedAccessUserGenerator.EXCLUSION_USER,
            LimitedAccessUserGenerator.RESTRICTION_USER,
            LimitedAccessUserGenerator.RESTRICTION_AND_EXCLUSION_USER
        )
        entityManager.persistAll(
            PersonGenerator.GENDER_MALE,
            PersonGenerator.ETHNICITY,
            PersonGenerator.PERSON_1,
            PersonGenerator.PERSON_2,
            PersonGenerator.EXCLUSION,
            PersonGenerator.RESTRICTION,
            PersonGenerator.RESTRICTION_EXCLUSION,
        )
        entityManager.flush()
        entityManager.persistAll(
            LimitedAccessGenerator.EXCLUSION,
            LimitedAccessGenerator.RESTRICTION,
            LimitedAccessGenerator.BOTH_EXCLUSION,
            LimitedAccessGenerator.BOTH_RESTRICTION,
        )
    }

    private fun EntityManager.persistAll(vararg entities: Any) {
        entities.forEach { persist(it) }
    }
}
