package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransferReasonRepository : JpaRepository<TransferReason, Long> {
    fun findByCode(code: String): TransferReason?
}
