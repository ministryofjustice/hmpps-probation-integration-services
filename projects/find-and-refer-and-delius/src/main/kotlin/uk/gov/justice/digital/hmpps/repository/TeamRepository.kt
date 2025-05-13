package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Team

interface TeamRepository : JpaRepository<Team, Long> {
    @Query(
        """
        select t
        from Team t
        join fetch t.district d
        where d.selectable = true
        and trim(d.borough.code) = :pduCode
        and trim(d.borough.provider.code) = :providerCode
        order by t.description
        """
    )
    fun getTeams(providerCode: String, pduCode: String): List<Team>
}
