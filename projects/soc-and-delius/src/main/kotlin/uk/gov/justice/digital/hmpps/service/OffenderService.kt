package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DetailRepository
import uk.gov.justice.digital.hmpps.model.probationrecord.ProbationRecord

@Service
class OffenderService(
    private val detailRepository: DetailRepository,
    private val objectMapper: ObjectMapper
) {
    fun getProbationRecord(crn: String): ProbationRecord {
        val json = detailRepository.getProbationRecord(crn)
        return objectMapper.readValue(json, ProbationRecord::class.java)
    }
}