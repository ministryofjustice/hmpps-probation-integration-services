package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.Data.BUSINESS_INTERACTIONS
import uk.gov.justice.digital.hmpps.data.generator.Data.CONTACT_TYPES
import uk.gov.justice.digital.hmpps.data.generator.Data.DUPLICATE_STAFF_1
import uk.gov.justice.digital.hmpps.data.generator.Data.DUPLICATE_STAFF_2
import uk.gov.justice.digital.hmpps.data.generator.Data.EVENT
import uk.gov.justice.digital.hmpps.data.generator.Data.MANAGER
import uk.gov.justice.digital.hmpps.data.generator.Data.MANAGER_STAFF
import uk.gov.justice.digital.hmpps.data.generator.Data.PERSON
import uk.gov.justice.digital.hmpps.data.generator.Data.STAFF
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
        listOf(
            PERSON,
            EVENT,
            STAFF,
            STAFF.user,
            DUPLICATE_STAFF_1,
            DUPLICATE_STAFF_1.user,
            DUPLICATE_STAFF_2,
            DUPLICATE_STAFF_2.user,
            MANAGER_STAFF,
            MANAGER,
            *CONTACT_TYPES,
            *BUSINESS_INTERACTIONS,
        ).forEach {
            entityManager.persist(it)
        }
    }
}
