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
        join t.localDeliveryUnit.probationDeliveryUnit.probationArea.approvedPremises ap
        where upper(t.localDeliveryUnit.description) like '%APPROVED PREMISES'
        and ap.code.code = :approvedPremisesCode
    """
    )
    fun findAllStaffLinkedToApprovedPremisesLDU(
        approvedPremisesCode: String,
        pageable: Pageable
    ): Page<Staff>
}
