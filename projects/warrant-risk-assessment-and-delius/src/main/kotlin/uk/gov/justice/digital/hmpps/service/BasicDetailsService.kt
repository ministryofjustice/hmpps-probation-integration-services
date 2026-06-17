package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.model.*

@Service
class BasicDetailsService(
    private val personRepository: PersonRepository,
    private val addressRepository: AddressRepository,
    private val personalContactRepository: PersonalContactRepository,
    private val contactRepository: ContactRepository,
) {
    fun getBasicDetails(crn: String): BasicDetails {
        val person = personRepository.getPerson(crn)
        val addresses = addressRepository.findByPersonIdAndEndDateIsNull(person.id)
        val employers = personalContactRepository.findCurrentEmployersByPersonId(person.id)
        val lastHomeVisitDate = contactRepository.findLastHomeVisitDate(person.id)

        return BasicDetails(
            title = person.title?.description,
            name = Name(
                forename = person.forename,
                middleName = listOfNotNull(person.secondName, person.thirdName).joinToString(" ").ifEmpty { null },
                surname = person.surname,
            ),
            dateOfBirth = person.dateOfBirth,
            nationalInsuranceNumber = person.niNumber,
            telephoneNumber = person.telephoneNumber,
            mobileNumber = person.mobileNumber,
            emailAddress = person.emailAddress,
            lastHomeVisitDate = lastHomeVisitDate,
            addresses = addresses.map { it.toModel() },
            employers = employers.map { it.toModel() },
        )
    }
}

private fun Address.toModel() = AddressDetail(
    id = id,
    status = status.description,
    buildingName = buildingName,
    buildingNumber = buildingNumber,
    streetName = streetName,
    townCity = townCity,
    district = district,
    county = county,
    postcode = postcode,
)

private fun PersonalContact.toModel() = Employer(
    employerName = Name(
        forename = forename,
        middleName = middleNames,
        surname = surname,
    ),
    employerAddress = address?.toModel(),
    telephoneNumber = address?.telephoneNumber,
    mobileNumber = mobileNumber,
)

private fun ContactAddress.toModel() = EmployerAddress(
    id = id,
    buildingName = buildingName,
    buildingNumber = buildingNumber,
    streetName = streetName,
    townCity = townCity,
    district = district,
    county = county,
    postcode = postcode,
)
