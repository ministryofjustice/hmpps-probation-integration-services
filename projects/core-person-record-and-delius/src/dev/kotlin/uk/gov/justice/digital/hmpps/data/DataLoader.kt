package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {
        saveAll(
            PersonGenerator.TITLE,
            PersonGenerator.GENDER,
            PersonGenerator.ETHNICITY,
            PersonGenerator.NATIONALITY,
            PersonGenerator.MAIN_ADDRESS,
            PersonGenerator.MIN_PERSON,
            PersonGenerator.FULL_PERSON,
            *PersonGenerator.FULL_PERSON_ALIASES.toTypedArray(),
            *PersonGenerator.FULL_PERSON_ADDRESSES.toTypedArray()
        )
    }

    private fun saveAll(vararg entities: Any) = entities.forEach(entityManager::merge)
}
