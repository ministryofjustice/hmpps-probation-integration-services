package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.DetailRepository
import uk.gov.justice.digital.hmpps.model.ProbationRecord

@Service
class OffenderService(detailRepository: DetailRepository) {
    fun getProbationRecord(crn: String): ProbationRecord {
        TODO()
    }
}