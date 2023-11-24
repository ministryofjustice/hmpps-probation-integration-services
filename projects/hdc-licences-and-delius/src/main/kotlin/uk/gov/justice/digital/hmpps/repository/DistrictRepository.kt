package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.District

interface DistrictRepository : JpaRepository<District, Long> {
    @Query(
        """
        select d 
        from District d
        join fetch d.borough b
        join fetch b.probationArea pa
        left join fetch d.teams
        where pa.code = :probationAreaCode
        and d.code = :code
        and d.selectable
        """
    )
    fun findByProbationAreaAndCode(probationAreaCode: String, code: String): District?
}
