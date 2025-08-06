package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.name
import uk.gov.justice.digital.hmpps.integrations.delius.toAddress
import uk.gov.justice.digital.hmpps.model.BasicDetails

@Service
@Transactional(readOnly = true)
class DetailsService(
    private val personRepository: PersonRepository
) {
    fun basicDetails(crn: String): BasicDetails {
       val person = personRepository.getByCrn(crn)

        return BasicDetails(
            title = person.title?.description,
            name = person.name(),
            prisonNumber = person.prisonerNumber,
            dateOfBirth = person.dateOfBirth,
            addresses = person.addresses.map { it.toAddress() }
        )
    }
}