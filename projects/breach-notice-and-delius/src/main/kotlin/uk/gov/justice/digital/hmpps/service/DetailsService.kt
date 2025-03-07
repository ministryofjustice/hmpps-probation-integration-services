package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.OfficeLocation
import uk.gov.justice.digital.hmpps.integrations.delius.Person
import uk.gov.justice.digital.hmpps.integrations.delius.PersonAddress
import uk.gov.justice.digital.hmpps.integrations.delius.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.StaffUserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.getByCrn
import uk.gov.justice.digital.hmpps.model.Address
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.model.Name

@Service
@Transactional(readOnly = true)
class DetailsService(
    private val personRepository: PersonRepository,
    private val staffUserRepository: StaffUserRepository
) {
    fun basicDetails(crn: String, username: String): BasicDetails =
        staffUserRepository.findByUsername(username)?.staff?.let {
            personRepository.getByCrn(crn).basicDetails(it)
        } ?: throw IllegalArgumentException("No staff found for $username")
}

fun Person.basicDetails(staff: Staff) = BasicDetails(
    title?.description,
    name(),
    addresses.map(PersonAddress::toAddress),
    staff.replyAddresses(),
)

fun Person.name() = Name(firstName, listOfNotNull(secondName, thirdName).joinToString(" "), surname)

fun PersonAddress.toAddress() = Address(
    id,
    status?.description,
    buildingName,
    buildingNumber,
    streetName,
    townCity,
    district,
    county,
    postcode,
)

fun OfficeLocation.toAddress() = Address(
    id,
    description,
    buildingName,
    buildingNumber,
    streetName,
    townCity,
    district,
    county,
    postcode,
)

fun Staff.replyAddresses() = teams.flatMap { team -> team.addresses.map { it.toAddress() } }