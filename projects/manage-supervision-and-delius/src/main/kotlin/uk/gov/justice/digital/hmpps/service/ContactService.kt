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
        val probationContacts = offenderManagerRepository.findOffenderManagersByPersonId(person.id)
        val prisonContacts = prisonManagerRepository.findPrisonManagersByPersonId(person.id)

        if (probationContacts.isEmpty()) {
            throw NotFoundException("Offender Manager records", "crn", crn)
        }

        return ProfessionalContact(
            person.toName(),
            probationContacts.map { it.toContact() } + prisonContacts.map {
            it.toContact() }.sortedByDescending { it.allocatedUntil }
        )
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
            responsibleOfficer = responsibleOfficer() != null,
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
            allocationDate= allocationDate,
            allocatedUntil = endDate,
            responsibleOfficer = responsibleOfficer() != null,
            prisonOffenderManager = true
        )
    }
}