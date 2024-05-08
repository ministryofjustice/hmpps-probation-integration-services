package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository

@Component
@ConditionalOnProperty("seed.database")
class ExistingAllocationsDataLoader(
    private val orderManagerRepository: OrderManagerRepository,
    private val existingAllocationsRefDataLoader: ExistingAllocationsRefDataLoader,
) {
    fun loadData() {
        existingAllocationsRefDataLoader.loadData()
        orderManagerRepository.save(OrderManagerGenerator.UNALLOCATED)
        orderManagerRepository.save(OrderManagerGenerator.INITIAL_ALLOCATION)
    }
}

@Component
@Transactional
class ExistingAllocationsRefDataLoader(private val entityManager: EntityManager) {
    fun loadData() {
        entityManager.persist(ProviderGenerator.PDU)
        entityManager.persist(ProviderGenerator.LAU)
        entityManager.persist(TeamGenerator.TEAM_IN_LAU)
        entityManager.persist(StaffGenerator.ALLOCATED)
        entityManager.persist(EventGenerator.HAS_INITIAL_ALLOCATION)
    }
}