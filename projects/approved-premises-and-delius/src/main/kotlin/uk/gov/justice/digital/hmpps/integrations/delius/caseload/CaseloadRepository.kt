package uk.gov.justice.digital.hmpps.integrations.delius.caseload

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    @Query(
        """
        select distinct c.team.code 
        from Caseload c
        left join c.team.staff s
        where c.person.crn = :crn
        and (:staffCode is null or :staffCode = s.code)
        """,
    )
    fun findTeamsManagingCase(
        crn: String,
        staffCode: String?,
    ): List<String>
}
