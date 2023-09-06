package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
        probationAreaId: Long,
        forename: String,
        surname: String
    ): Staff?

    @Query(
        """
        select officer_code from staff
        where regexp_like(officer_code, ?1, 'i')
        order by officer_code desc
        fetch next 1 rows only
        """,
        nativeQuery = true
    )
    fun getLatestStaffReference(regex: String): String?
}
