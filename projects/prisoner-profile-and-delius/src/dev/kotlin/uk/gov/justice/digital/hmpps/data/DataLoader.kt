package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.ALLOCATED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.JAMES_BROWN
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.STAFF
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.UNALLOCATED_PERSON
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.UNALLOCATED_STAFF
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator.generateCommunityManager
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
        listOf(
            TEAM,
            UNALLOCATED_STAFF,
            STAFF,
            JAMES_BROWN,
            UNALLOCATED_PERSON,
            ALLOCATED_PERSON,
            generateCommunityManager(
                ALLOCATED_PERSON,
                STAFF
            ),
            generateCommunityManager(
                UNALLOCATED_PERSON,
                UNALLOCATED_STAFF
            )
        ).saveAll()
    }

    fun List<Any>.saveAll() = forEach { entityManager.persist(it) }
}
