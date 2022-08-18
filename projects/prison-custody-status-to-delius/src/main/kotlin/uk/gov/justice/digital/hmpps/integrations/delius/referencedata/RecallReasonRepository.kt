package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.springframework.data.jpa.repository.JpaRepository

interface RecallReasonRepository : JpaRepository<RecallReason, Long> {
    fun findByCodeAndSelectableIsTrue(code: String): RecallReason
}
