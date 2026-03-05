package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.Event

interface EventRepository : JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = ["disposal.type", "disposal.requirements.mainCategory.lengthUnits", "disposal.requirements.subCategory"])
    fun findByPersonIdAndDisposalNotNull(personId: Long): List<Event>
}