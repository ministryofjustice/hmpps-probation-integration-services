package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnit

interface PduRepository : JpaRepository<ProbationDeliveryUnit, Long> {
    @Query(
        """
        select pdu from ProbationDeliveryUnit pdu
        where pdu.provider.code = :code
          and pdu.selectable = true
          and pdu.provider.selectable = true
        order by pdu.code
        """
    )
    fun findByProviderCodeAndSelectableTrueAndProviderSelectableTrue(code: String): List<ProbationDeliveryUnit>
}