package uk.gov.justice.digital.hmpps.integrations.delius.staff

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface StaffRepository : JpaRepository<Staff, Long> {
    @Query(
        """
        select distinct s from Staff s
        join s.teams t
        join t.approvedPremises ap
        where ap.code.code = :approvedPremisesCode
        """
    )
    fun findAllStaffLinkedToApprovedPremisesTeam(
        approvedPremisesCode: String,
        pageable: Pageable
    ): Page<Staff>

    @Query(
        """
        select distinct s from Staff s
        join s.approvedPremises ap
        where ap.code.code = :approvedPremisesCode
        """
    )
    fun findKeyWorkersLinkedToApprovedPremises(
        approvedPremisesCode: String,
        pageable: Pageable
    ): Page<Staff>

    fun findByCode(code: String): Staff?

    @EntityGraph(attributePaths = ["user"])
    fun findByUserUsername(username: String): Staff?
}

fun StaffRepository.getByCode(code: String): Staff =
    findByCode(code) ?: throw NotFoundException("Staff", "code", code)

fun StaffRepository.getUnallocated(teamCode: String): Staff =
    findByCode(teamCode + "U") ?: throw NotFoundException("Unable to find unallocated staff for $teamCode")
