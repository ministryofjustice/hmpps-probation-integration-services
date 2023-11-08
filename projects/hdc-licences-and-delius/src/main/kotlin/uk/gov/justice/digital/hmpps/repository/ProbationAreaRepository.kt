package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.ProbationArea

interface ProbationAreaRepository : JpaRepository<ProbationArea, Long> {
    @Query(
        """
        select pa 
        from ProbationArea pa 
        where pa.selectable = true
        and (pa.establishment is null or pa.establishment)
        """
    )
    fun findSelectableProbationAreas(): List<ProbationArea>

    @Query(
        """
        select pa 
        from ProbationArea pa 
        left join fetch pa.boroughs b
        left join fetch b.districts d
        where pa.code = :code
        and (d is null or d.selectable)
        """
    )
    fun findByCodeWithSelectableDistricts(code: String): ProbationArea?
}
