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
    private val entityManager: EntityManager,
    private val auditUserRepository: AuditUserRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }



    @Transactional
    override fun onApplicationEvent(are: ApplicationReadyEvent) {

        entityManager.persist(PersonGenerator.OVERVIEW.gender)
        entityManager.persist(PersonGenerator.OVERVIEW.ethnicity)
        entityManager.persist(PersonGenerator.OVERVIEW.primaryLanguage)

        PersonGenerator.OVERVIEW.disabilities.forEach{entityManager.persist(it.type)}
        PersonGenerator.OVERVIEW.provisions.forEach{entityManager.persist(it.type)}
        PersonGenerator.OVERVIEW.personalCircumstances.forEach{
            entityManager.persist(it.type)
            entityManager.persist(it.subType)
        }

        entityManager.persistList(PersonGenerator.OVERVIEW.disabilities)
        entityManager.persistList(PersonGenerator.OVERVIEW.provisions)
        entityManager.persistList(PersonGenerator.OVERVIEW.personalCircumstances)
        entityManager.persist(PersonGenerator.OVERVIEW)
    }



    private fun EntityManager.persistList(entities: List<Any>) {
        entities.forEach { persist(it) }
    }
}
