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

        val activePersonManagers = getActivePersonManagers(person.id)

        if (activePersonManagers.isEmpty()) {
            throw NotFoundException("Offender Manager records", "crn", crn)
        }

        return ProfessionalContact(
            person.toName(),
            activePersonManagers,
            getInactiveCommunityManagers(person.id).sortedByDescending { it.allocatedUntil },
        )
    }

    fun getInactiveCommunityManagers(id: Long): List<Contact> {
        val probationContacts = offenderManagerRepository.findOffenderManagersByPersonIdAndActiveIsFalse(id)

        return probationContacts.map { it.toContact() }
    }

    fun getActivePersonManagers(id: Long): List<Contact> {
        val communityManager = offenderManagerRepository.findOffenderManagersByPersonIdAndActiveIsTrue(id)
        val prisonManager = prisonManagerRepository.findPrisonManagerByPersonId(id)

        return listOfNotNull(
            communityManager?.toContact(),
            prisonManager?.toContact()
        ).sortedByDescending { it.allocationDate }
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
            prisonOffenderManager = false,
            isUnallocated = staff.code.endsWith("U")
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
            prisonOffenderManager = true,
            isUnallocated = staff.code.endsWith("U")
        )
    }
}