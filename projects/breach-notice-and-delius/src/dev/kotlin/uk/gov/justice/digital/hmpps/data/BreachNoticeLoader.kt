package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class BreachNoticeLoader(
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
            *WarningGenerator.WARNING_TYPES.toTypedArray(),
            PersonGenerator.DEFAULT_ADDRESS_TYPE,
            PersonGenerator.DEFAULT_PERSON,
            PersonGenerator.DEFAULT_ADDRESS,
            TeamGenerator.DEFAULT_LOCATION,
            TeamGenerator.DEFAULT_TEAM,
            StaffGenerator.DEFAULT_STAFF,
            StaffGenerator.DEFAULT_SU,
        )
    }

    private fun EntityManager.persistAll(vararg entities: Any) {
        entities.forEach { persist(it) }
    }
}
