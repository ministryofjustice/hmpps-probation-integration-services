package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
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
        loadProviders()
        loadTeams()
    }

    fun loadProviders() {
        entityManager.persist(ProviderGenerator.DEFAULT_PROVIDER)
        entityManager.persist(ProviderGenerator.SECOND_PROVIDER)
    }

    fun loadTeams() {
        entityManager.persist(TeamGenerator.DEFAULT_UPW_TEAM)
        entityManager.persist(TeamGenerator.SECOND_UPW_TEAM)
        entityManager.persist(TeamGenerator.NON_UPW_TEAM)
        entityManager.persist(TeamGenerator.END_DATED_TEAM)
        entityManager.persist(TeamGenerator.OTHER_PROVIDER_TEAM)
    }
}
