package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.entity.AdditionalSentence

interface AdditionalSentenceRepository : JpaRepository<AdditionalSentence, Long> {
    fun findAllByEvent_IdIn(eventIds: List<Long>): List<AdditionalSentence>
}