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
            PersonGenerator.DS_ADDRESS_TYPE,
            PersonGenerator.DEFAULT_ADDRESS_TYPE,
            WarningGenerator.DS_BREACH_NOTICE_TYPE,
            *WarningGenerator.NOTICE_TYPES.toTypedArray(),
            WarningGenerator.DS_BREACH_REASON,
            *WarningGenerator.BREACH_REASONS.toTypedArray(),
            WarningGenerator.DS_BREACH_CONDITION_TYPE,
            *WarningGenerator.CONDITION_TYPES.toTypedArray(),
            WarningGenerator.DS_BREACH_SENTENCE_TYPE,
            *WarningGenerator.SENTENCE_TYPES.toTypedArray(),
            WarningGenerator.ENFORCEABLE_CONTACT_TYPE,
            WarningGenerator.ENFORCEABLE_CONTACT_OUTCOME,
            TeamGenerator.DEFAULT_LOCATION,
            TeamGenerator.DEFAULT_TEAM,
            StaffGenerator.DEFAULT_STAFF,
            StaffGenerator.DEFAULT_SU,
            PersonGenerator.DEFAULT_PERSON,
            PersonGenerator.DEFAULT_ADDRESS,
            WarningGenerator.DEFAULT_EVENT,
            WarningGenerator.DEFAULT_DISPOSAL,
            WarningGenerator.DEFAULT_RQMNT_CATEGORY,
            WarningGenerator.DS_REQUIREMENT_SUB_CATEOGORY,
            WarningGenerator.DEFAULT_RQMNT_SUB_CATEGORY,
            WarningGenerator.DEFAULT_RQMNT,
            WarningGenerator.DEFAULT_ENFORCEABLE_CONTACT,
        )
    }

    private fun EntityManager.persistAll(vararg entities: Any) {
        entities.forEach { persist(it) }
    }
}
