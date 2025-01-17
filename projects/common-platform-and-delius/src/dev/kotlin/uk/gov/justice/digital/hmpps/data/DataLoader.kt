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
        entityManager.run {
            persist(BusinessInteractionGenerator.INSERT_PERSON)
            persist(BusinessInteractionGenerator.INSERT_ADDRESS)
            persist(DatasetGenerator.GENDER)
            persist(DatasetGenerator.OM_ALLOCATION_REASON)
            persist(DatasetGenerator.ADDRESS_STATUS)
            persist(DatasetGenerator.ADDRESS_TYPE)
            persist(ReferenceDataGenerator.GENDER_MALE)
            persist(ReferenceDataGenerator.GENDER_FEMALE)
            persist(ReferenceDataGenerator.INITIAL_ALLOCATION)
            persist(ReferenceDataGenerator.MAIN_ADDRESS_STATUS)
            persist(ReferenceDataGenerator.AWAITING_ASSESSMENT)
            persist(ProviderGenerator.DEFAULT)
            persist(TeamGenerator.ALLOCATED)
            persist(TeamGenerator.UNALLOCATED)
            persist(StaffGenerator.UNALLOCATED)
            persist(StaffGenerator.ALLOCATED)
            persist(CourtGenerator.UNKNOWN_COURT_N07_PROVIDER)
        }
    }
}
