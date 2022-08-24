package uk.gov.justice.digital.hmpps.integrations.delius.recall.reason

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface RecallReasonRepository : JpaRepository<RecallReason, Long> {
    fun findByCodeAndSelectableIsTrue(code: String): RecallReason?
}

fun RecallReasonRepository.getByCodeAndSelectableIsTrue(code: String): RecallReason =
    findByCodeAndSelectableIsTrue(code) ?: throw NotFoundException("RecallReason", "code", code)
