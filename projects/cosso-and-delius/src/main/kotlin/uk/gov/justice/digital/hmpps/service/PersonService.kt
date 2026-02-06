package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.PersonAddressRepository
import uk.gov.justice.digital.hmpps.entity.PersonRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Address
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.model.PersonDetails

@Service
class PersonService(
    val personRepository: PersonRepository,
    val personAddressRepository: PersonAddressRepository
) {
    companion object {
        const val MAIN_ADDRESS_STATUS = "MAIN"
    }

    fun getBasicDetails(crn: String): PersonDetails {
        val person = personRepository.findByCrn(crn) ?: throw NotFoundException("Person with crn:$crn not found")
        val addresses = personAddressRepository.findByPersonId(person.offenderId).map {
            Address(
                id = it.id,
                status = it.status.code,
                buildingName = it.buildingName,
                buildingNumber = it.addressNumber,
                streetName = it.streetName,
                townCity = it.townCity,
                district = it.district,
                county = it.county,
                postcode = it.postcode,
            )
        }
        if (addresses.none { it.status.uppercase() == MAIN_ADDRESS_STATUS }) {
            throw NotFoundException("No main address found for person with crn:$crn")
        }
        return PersonDetails(
            title = person.title?.description ?: "",
            name = Name(person.firstName, person.middleName(), person.surname),
            dateOfBirth = person.dateOfBirth,
            telephoneNumber = person.telephoneNumber,
            mobileNumber = person.mobileNumber,
            emailAddress = person.emailAddress,
            addresses = addresses
        )
    }
}
