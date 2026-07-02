package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.Address.Companion.toModel
import uk.gov.justice.digital.hmpps.model.Manager
import uk.gov.justice.digital.hmpps.model.OfficeAddress.Companion.toModel
import uk.gov.justice.digital.hmpps.model.PersonalContact.Companion.toModel
import uk.gov.justice.digital.hmpps.model.PersonalDetails
import uk.gov.justice.digital.hmpps.model.Team
import uk.gov.justice.digital.hmpps.repository.PersonAddressRepository
import uk.gov.justice.digital.hmpps.repository.PersonRepository
import uk.gov.justice.digital.hmpps.repository.PersonalContactRepository

@Service
class PersonalDetailsService(
    private val personRepository: PersonRepository,
    private val personAddressRepository: PersonAddressRepository,
    private val personalContactRepository: PersonalContactRepository,
) {
    fun getName(crn: String) = personRepository.getNameByCrn(crn)

    fun getPersonalDetails(crn: String): PersonalDetails {
        val person = personRepository.getByCrn(crn)
        val mainAddress = personAddressRepository.findMainAddress(person.id)
        val emergencyContacts = personalContactRepository.findEmergencyContacts(person.id)
        return PersonalDetails(
            name = person.name(),
            preferredName = person.preferredName,
            dateOfBirth = person.dateOfBirth,
            lastUpdatedAt = person.lastUpdatedDatetime,
            mainAddress = mainAddress?.toModel(),
            telephoneNumber = person.telephoneNumber,
            mobileNumber = person.mobileNumber,
            emailAddress = person.emailAddress,
            emergencyContacts = emergencyContacts.map { it.toModel() },
            practitioner = Manager(
                name = person.manager.staff.name(),
                team = with(person.manager.team) { Team(telephone, officeLocations.map { it.toModel() }) }
            )
        )
    }
}
