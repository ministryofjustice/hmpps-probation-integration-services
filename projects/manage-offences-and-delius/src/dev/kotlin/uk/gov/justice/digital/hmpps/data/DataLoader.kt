package uk.gov.justice.digital.hmpps.data

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.repository.DetailedOffenceRepository
import uk.gov.justice.digital.hmpps.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.user.AuditUserRepository

@Component
@ConditionalOnProperty("seed.database")
class DataLoader(
    private val auditUserRepository: AuditUserRepository,
    private val entityManager: EntityManager,
    private val referenceDataRepository: ReferenceDataRepository,
    private val detailedOffenceRepository: DetailedOffenceRepository
) : ApplicationListener<ApplicationReadyEvent> {

    @PostConstruct
    fun saveAuditUser() {
        auditUserRepository.save(UserGenerator.AUDIT_USER)
    }

    @Transactional
    override fun onApplicationEvent(applicationReadyEvent: ApplicationReadyEvent) {
        entityManager.persist(DataGenerator.COURT_CATEGORY_SET)
        referenceDataRepository.save(DataGenerator.COURT_CATEGORY)
        detailedOffenceRepository.save(DataGenerator.EXISTING_OFFENCE)
    }
}
