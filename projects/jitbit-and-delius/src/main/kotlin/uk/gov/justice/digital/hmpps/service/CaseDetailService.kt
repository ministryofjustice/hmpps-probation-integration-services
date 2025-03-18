package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.Address
import uk.gov.justice.digital.hmpps.model.CaseDetails
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.repository.AddressRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.findMainAddress
import uk.gov.justice.digital.hmpps.repository.getPerson

@Service
class CaseDetailService(
    private val personRepository: PersonRepository,
    private val addressRepository: AddressRepository
) {
    fun getCaseDetails(crn: String): CaseDetails {
        val person = personRepository.getPerson(crn)
        val mainAddress = addressRepository.findMainAddress(person.id)
        return CaseDetails(
            name = Name(
                forename = person.forename,
                middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" ").ifEmpty { null },
                surname = person.surname,
            ),
            dateOfBirth = person.dateOfBirth,
            mainAddress = mainAddress?.let {
                Address(
                    buildingName = it.buildingName,
                    addressNumber = it.addressNumber,
                    streetName = it.streetName,
                    townCity = it.townCity,
                    district = it.district,
                    county = it.county,
                    postcode = it.postcode,
                    noFixedAbode = it.noFixedAbode,
                )
            }
        )
    }
}
