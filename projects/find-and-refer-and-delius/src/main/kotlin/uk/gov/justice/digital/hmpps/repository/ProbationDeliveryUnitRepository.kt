package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Borough

interface ProbationDeliveryUnitRepository : JpaRepository<Borough, Long> {
    @Query(
        """
        select b
        from Borough b
        join fetch b.districts d
        join fetch d.teams teams
        where d.selectable = true
        and trim(b.provider.code) = :providerCode
        order by b.description asc
        """
    )
    fun findPdus(providerCode: String): List<Borough>
}
