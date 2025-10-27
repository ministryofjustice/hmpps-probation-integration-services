package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
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
        loadUsers()
        loadProviders()
        loadTeams()
        loadStaff()
    }

    fun loadUsers() {
        entityManager.persist(UserGenerator.DEFAULT_USER)
    }

    fun loadProviders() {
        entityManager.persist(ProviderGenerator.DEFAULT_PROVIDER)
        entityManager.persist(ProviderGenerator.SECOND_PROVIDER)
        entityManager.persist(ProviderGenerator.UNSELECTABLE_PROVIDER)

        entityManager.persist(ProviderGenerator.DEFAULT_PROBATION_AREA_USER)
        entityManager.persist(ProviderGenerator.SECOND_DEFAULT_PROBATION_AREA_USER)
        entityManager.persist(ProviderGenerator.DEFAULT_USER_UNSELECTABLE_PROBATION_AREA)
    }

    fun loadTeams() {
        entityManager.persist(TeamGenerator.DEFAULT_UPW_TEAM)
        entityManager.persist(TeamGenerator.SECOND_UPW_TEAM)
        entityManager.persist(TeamGenerator.NON_UPW_TEAM)
        entityManager.persist(TeamGenerator.END_DATED_TEAM)
        entityManager.persist(TeamGenerator.OTHER_PROVIDER_TEAM)
    }

    fun loadStaff() {
        entityManager.persist(StaffGenerator.DEFAULT_STAFF)
        entityManager.persist(StaffGenerator.SECOND_STAFF)
    }
}
