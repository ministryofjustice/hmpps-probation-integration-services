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
        select officer_code from staff
        where officer_code like :regex || '%'
        and substr(officer_code,5,1) in ('0','1','2','3','4','5','6','7','8','9')
        and substr(officer_code,6,1) in ('0','1','2','3','4','5','6','7','8','9')
        and substr(officer_code,7,1) in ('0','1','2','3','4','5','6','7','8','9')
        order by officer_code desc
        fetch next 1 rows only
        """,
        nativeQuery = true
    )
    fun getLatestStaffReference(regex: String): String?

    fun findByCode(code: String): PrisonStaff?
}

fun PrisonStaffRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)
