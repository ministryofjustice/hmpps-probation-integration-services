package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.sentence.offence.OffenceEvent

interface OffenceRepository : JpaRepository<OffenceEvent, Long> {
    @EntityGraph(
        attributePaths = [
            "person",
            "additionalOffences.offence"
        ]
    )
    fun findByPersonCrnAndNumber(crn: String, eventNumber: String): OffenceEvent?
}