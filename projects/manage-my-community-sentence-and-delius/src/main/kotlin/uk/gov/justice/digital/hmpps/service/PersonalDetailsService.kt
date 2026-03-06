package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.ldap.findAttributeByUsername
import uk.gov.justice.digital.hmpps.model.Address.Companion.toModel
import uk.gov.justice.digital.hmpps.model.Manager
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
    private val ldapTemplate: LdapTemplate
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
            mainAddress = mainAddress?.toModel(),
            telephoneNumber = person.telephoneNumber,
            mobileNumber = person.mobileNumber,
            emailAddress = person.emailAddress,
            emergencyContacts = emergencyContacts.map { it.toModel() },
            practitioner = Manager(
                name = person.manager.staff.name(),
                telephoneNumber = person.manager.staff.user?.username?.let {
                    ldapTemplate.findAttributeByUsername(it, "telephoneNumber")
                },
                team = Team(person.manager.team.officeLocations.map { it.toModel() })
            )
        )
    }
}
