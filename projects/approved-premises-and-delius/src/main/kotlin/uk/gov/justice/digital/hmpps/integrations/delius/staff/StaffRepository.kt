package uk.gov.justice.digital.hmpps.integrations.delius.staff

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StaffRepository : JpaRepository<Staff, Long> {
    @Query(
        """
        select s from Staff s
        join s.teams t
        join t.localAdminUnit.probationDeliveryUnit.probationArea.approvedPremises ap
        where upper(t.localAdminUnit.description) like '%APPROVED PREMISES'
        and ap.code.code = :approvedPremisesCode
        """
    )
    fun findAllStaffLinkedToApprovedPremisesLAU(
        approvedPremisesCode: String,
        pageable: Pageable
    ): Page<Staff>
}
