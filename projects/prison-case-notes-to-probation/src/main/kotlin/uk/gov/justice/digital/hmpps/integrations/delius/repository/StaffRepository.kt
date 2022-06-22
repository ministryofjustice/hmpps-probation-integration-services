package uk.gov.justice.digital.hmpps.integrations.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
        probationAreaId: Long,
        forename: String,
        surname: String
    ): Staff?

    @Query(
        """
         select officer_code from staff 
         where regexp_like(officer_code,'^:prefix[[:digit:]]{3}$')
         order by 1 desc
        """, nativeQuery = true
    )
    fun getLatestStaffReference(prefix: String): String?
}