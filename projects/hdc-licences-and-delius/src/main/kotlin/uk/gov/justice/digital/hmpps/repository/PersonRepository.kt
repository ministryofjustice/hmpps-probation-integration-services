package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.Person

interface PersonRepository : JpaRepository<Person, Long> {
    @Query(
        """
            select distinct noms_number 
            from offender
            where offender_id in (
                select offender_id from offender_manager 
                where allocation_staff_id = :staffId and active_flag = 1 and soft_deleted = 0
                union
                select offender_id from prison_offender_manager 
                where allocation_staff_id = :staffId and active_flag = 1 and soft_deleted = 0
            )
        """,
        nativeQuery = true
    )
    fun findManagedPrisonerIdentifiersByStaffId(staffId: Long): List<String>

    @Query(
        """
            select distinct noms_number 
            from offender
            where offender_id in (
                select offender_id from offender_manager om
                join staff s on s.staff_id = om.allocation_staff_id
                where s.officer_code = :code and om.active_flag = 1 and om.soft_deleted = 0
                union
                select offender_id from prison_offender_manager pom
                join staff s on s.staff_id = pom.allocation_staff_id
                where s.officer_code = :code and pom.active_flag = 1 and pom.soft_deleted = 0
            )
        """,
        nativeQuery = true
    )
    fun findManagedPrisonerIdentifiersByStaffCode(code: String): List<String>
}
