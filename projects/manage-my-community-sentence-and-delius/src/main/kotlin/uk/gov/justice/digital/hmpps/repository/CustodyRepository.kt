package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.Custody

interface CustodyRepository : JpaRepository<Custody, Long> {
    @EntityGraph(attributePaths = ["sentenceExpiryDates"])
    fun findByDisposalId(disposalId: Long): Custody?
}
