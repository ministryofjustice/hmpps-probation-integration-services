package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.PersonRepository

@Service
class ContactDetailsService(
    val personRepository: PersonRepository
) {
    fun getContactDetailsForCrn(crn: String) =
        personRepository.findByCrn(crn)?.let { person ->
            uk.gov.justice.digital.hmpps.model.ContactDetails(
                crn = person.crn,
                name = uk.gov.justice.digital.hmpps.model.Name(
                    forename = person.firstName,
                    surname = person.lastName
                ),
                mobile = person.mobile,
                email = person.emailAddress
            )
        }

    fun getContactDetailsForCrns(crns: List<String>) =
        personRepository.findByCrnIn(crns).map { person ->
            uk.gov.justice.digital.hmpps.model.ContactDetails(
                crn = person.crn,
                name = uk.gov.justice.digital.hmpps.model.Name(
                    forename = person.firstName,
                    surname = person.lastName
                ),
                mobile = person.mobile,
                email = person.emailAddress
            )
        }
}