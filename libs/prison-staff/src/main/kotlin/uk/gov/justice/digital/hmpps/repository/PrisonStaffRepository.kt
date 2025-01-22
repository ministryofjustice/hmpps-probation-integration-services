package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.PrisonStaff
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface PrisonStaffRepository : JpaRepository<PrisonStaff, Long> {
    fun findTopByProbationAreaIdAndForenameIgnoreCaseAndSurnameIgnoreCase(
        probationAreaId: Long,
        forename: String,
        surname: String
    ): PrisonStaff?

    @Query(
        """
        select min(next_officer_code)
        from ( select officer_code,
              substr(officer_code, 1, 3) ||
              case when cast(substr(officer_code, 5, 3) as number) = 999 then chr(ascii(substr(officer_code, 4, 1)) + 1)
                   else substr(officer_code, 4, 1) end ||
              to_char(case when cast(substr(officer_code, 5, 3) as number) = 999 then 000
                           else cast(substr(officer_code, 5, 3) as number) + 1 end, 'FM000') next_officer_code
                from staff
                where officer_code like :regex || '%'
                and substr(officer_code, 5, 1) in ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                and substr(officer_code, 6, 1) in ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                and substr(officer_code, 7, 1) in ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9') )
        where next_officer_code not in ( select officer_code from staff);
        """,
        nativeQuery = true
    )
    fun getNextStaffReference(regex: String): String?

    fun findByCode(code: String): PrisonStaff?
}

fun PrisonStaffRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)
