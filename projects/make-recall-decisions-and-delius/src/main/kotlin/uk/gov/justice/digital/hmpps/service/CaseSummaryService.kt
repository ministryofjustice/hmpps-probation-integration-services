package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.PersonalDetails
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryAddress
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryAddressRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPerson
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.CaseSummaryPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.findMainAddress
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.getPerson

@Service
class CaseSummaryService(
    private val personRepository: CaseSummaryPersonRepository,
    private val addressRepository: CaseSummaryAddressRepository
) {
    fun getPersonalDetails(crn: String): PersonalDetails {
        val person = personRepository.getPerson(crn)
        val mainAddress = addressRepository.findMainAddress(person.id)
        return PersonalDetails(
            name = person.name(),
            identifiers = person.identifiers(),
            dateOfBirth = person.dateOfBirth,
            gender = person.gender.description,
            ethnicity = person.ethnicity?.description,
            primaryLanguage = person.primaryLanguage?.description,
            mainAddress = mainAddress?.address()
        )
    }

    private fun CaseSummaryPerson.name() = PersonalDetails.Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
    private fun CaseSummaryPerson.identifiers() = PersonalDetails.Identifiers(pncNumber, croNumber, nomsNumber, bookingNumber = mostRecentPrisonerNumber)
    private fun CaseSummaryAddress.address() = PersonalDetails.Address(
        buildingName = buildingName,
        addressNumber = addressNumber,
        streetName = streetName,
        town = town,
        county = county,
        postcode = postcode,
        noFixedAbode = noFixedAbode
    )
}
