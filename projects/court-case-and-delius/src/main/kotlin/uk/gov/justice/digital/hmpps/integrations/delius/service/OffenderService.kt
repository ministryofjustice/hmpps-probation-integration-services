package uk.gov.justice.digital.hmpps.integrations.delius.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository

@Service
class OffenderService(
    private val detailRepository: PersonRepository,
    private val objectMapper: ObjectMapper
) {
    fun getProbationRecord(crn: String): ProbationRecord {
        val json = detailRepository.getProbationRecord(crn)
        return objectMapper.readValue(json, ProbationRecord::class.java)
    }
}
