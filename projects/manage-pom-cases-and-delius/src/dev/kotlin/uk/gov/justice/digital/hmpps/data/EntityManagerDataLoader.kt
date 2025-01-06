package uk.gov.justice.digital.hmpps.data

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff

@Component
class EntityManagerDataLoader{

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun loadData():Map<String, Staff> {

        val staffMap = (PersonManagerGenerator.ALL.map { it.staff } + ProviderGenerator.UNALLOCATED_STAFF).associateBy { it.code }

        val savedStaffMap = staffMap.map {
            entityManager.merge(it.value)
        }.associateBy { it.code }

        return savedStaffMap

    }

}