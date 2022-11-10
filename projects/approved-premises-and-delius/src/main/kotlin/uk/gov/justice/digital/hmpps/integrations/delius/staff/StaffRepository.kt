package uk.gov.justice.digital.hmpps.integrations.delius.staff

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

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
}
