package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.ProbationStatusDetail
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository

@Service
class ProbationStatusService(private val personRepository: PersonRepository) {
    fun getProbationStatus(crn: String): ProbationStatusDetail =
        ProbationStatusDetail(personRepository.managedStatus(crn))
}
