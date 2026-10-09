package uk.gov.justice.digital.hmpps.entity.event.sentence

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface LicenceConditionRepository : JpaRepository<LicenceCondition, Long> {
    @EntityGraph(attributePaths = ["mainCategory", "subCategory"])
    fun findByDisposalIdIn(disposalIds: Collection<Long>): List<LicenceCondition>
}