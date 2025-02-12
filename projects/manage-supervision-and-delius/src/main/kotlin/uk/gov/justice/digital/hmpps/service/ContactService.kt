package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.Contact
import uk.gov.justice.digital.hmpps.api.model.sentence.ProfessionalContact
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.getPerson
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class ContactService(
    private val personRepository: PersonRepository,
    private val offenderManagerRepository: OffenderManagerRepository,
    private val prisonManagerRepository: PrisonManagerRepository,
    private val ldapTemplate: LdapTemplate
) {

    fun getContacts(crn: String): ProfessionalContact {
        val person = personRepository.getPerson(crn)

        val combinedContacts = getCombinedContacts(person.id)

        if (combinedContacts.isEmpty()) {
            throw NotFoundException("Offender Manager records", "crn", crn)
        }

        return ProfessionalContact(
            person.toName(),
            combinedContacts.filter { it.allocatedUntil == null }.sortedByDescending { it.allocationDate },
            combinedContacts.filter { it.allocatedUntil != null }.sortedByDescending { it.allocatedUntil },
        )
    }

    fun getCombinedContacts(id: Long): List<Contact> {
        val probationContacts = offenderManagerRepository.findOffenderManagersByPersonId(id)
        val prisonContacts = prisonManagerRepository.findPrisonManagersByPersonId(id)

        return probationContacts.map { it.toContact() } + prisonContacts.map {
            it.toContact()
        }
    }

    fun Person.toName() =
        Name(forename, secondName, surname)

    fun OffenderManager.toContact(): Contact {
        staff.user?.apply {
            ldapTemplate.findByUsername<LdapUser>(username)?.let {
                email = it.email
                telephone = it.telephone
            }
        }
        return Contact(
            staff.forename + " " + staff.surname,
            staff.user?.email,
            staff.user?.telephone,
            provider.description,
            team.district.borough.description,
            team.description,
            allocationDate = allocationDate,
            allocatedUntil = endDate,
            lastUpdated = lastUpdated.toLocalDate(),
            responsibleOfficer = responsibleOfficer != null,
            prisonOffenderManager = false
        )
    }

    fun PrisonManager.toContact(): Contact {
        staff.user?.apply {
            ldapTemplate.findByUsername<LdapUser>(username)?.let {
                email = it.email
                telephone = it.telephone
            }
        }
        return Contact(
            staff.forename + " " + staff.surname,
            staff.user?.email,
            staff.user?.telephone,
            provider.description,
            team.district.borough.description,
            team.description,
            allocationDate = allocationDate,
            allocatedUntil = endDate,
            lastUpdated = lastUpdated.toLocalDate(),
            responsibleOfficer = responsibleOfficer != null,
            prisonOffenderManager = true
        )
    }
}