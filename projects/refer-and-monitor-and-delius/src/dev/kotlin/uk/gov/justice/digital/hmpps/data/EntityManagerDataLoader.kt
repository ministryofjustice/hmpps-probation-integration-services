package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator.NSI_FUZZY_SEARCH

@Component
class EntityManagerDataLoader {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun loadData() {
        NSI_FUZZY_SEARCH = entityManager.merge(NsiGenerator.FUZZY_SEARCH)
        entityManager.merge(NsiGenerator.TERMINATED)
    }
}