package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Requirement

interface RequirementRepository : JpaRepository<Requirement, Long> {

    @Query(
        """
        select r
        from Requirement r
        left join fetch r.mainCategory mc
        left join fetch r.subCategory sc
        left join fetch r.additionalMainCategory amc
        left join fetch r.terminationDetails td
        where r.person.id = :personId
        and (mc.code = 'RM38' 
            or (mc.code = '7' and (sc is null or sc.code <> 'RS66'))
            or (amc.code in ('RM38', '7')))
        and r.mainCategory.id is not null 
        and r.softDeleted = false
        order by r.endDate desc, r.startDate desc 
    """
    )
    fun getAccreditedProgrammeHistory(personId: Long): List<Requirement>
}
