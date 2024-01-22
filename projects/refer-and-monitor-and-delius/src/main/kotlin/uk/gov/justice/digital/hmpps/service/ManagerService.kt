package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseIdentifier
import uk.gov.justice.digital.hmpps.api.model.ManagedCases
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.Pdu
import uk.gov.justice.digital.hmpps.api.model.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.*
import uk.gov.justice.digital.hmpps.integrations.ldap.entity.LdapUserDetails
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class ManagerService(
    private val personManagerRepository: PersonManagerRepository,
    private val prisonManagerRepository: PrisonManagerRepository,
    private val locationRepository: LocationRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun findResponsibleCommunityManager(crn: String): ResponsibleOfficer {
        val com = personManagerRepository.findByPersonCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        com.staff.user?.apply {
            ldapTemplate.findByUsername<LdapUserDetails>(username)?.let {
                email = it.email
                telephone = it.telephone
            }
        }
        val locations = locationRepository.findLocationsForTeam(com.team.id)
        val pom = if (com.responsibleOfficer == null) prisonManagerRepository.findByPersonId(com.person.id) else null
        return com.toResponsibleOfficer(locations, pom)
    }

    fun findCasesManagedBy(username: String) = ManagedCases(
        (personManagerRepository.findCasesManagedBy(username) + personManagerRepository.findCasesManagedBy(username)).toSet()
            .map { CaseIdentifier(it) }
    )
}

fun PersonManager.toResponsibleOfficer(locations: List<Location>, pom: PrisonManager?) = ResponsibleOfficer(
    Manager(
        staff.code,
        staff.name(),
        staff.user?.username,
        staff.user?.email,
        staff.user?.telephone,
        responsibleOfficer != null,
        team.pdu(),
        team.team(),
        officeLocations = locations.map(Location::location)
    ),
    pom?.toManager()
)

fun PrisonManager.toManager() =
    Manager(
        staff.code,
        staff.name(),
        null,
        emailAddress,
        telephoneNumber,
        responsibleOfficer != null,
        team.pdu(),
        team.team()
    )

fun Staff.name() = Name(forename, surname)

fun Team.team() = uk.gov.justice.digital.hmpps.api.model.Team(code, description, email, telephone)
fun Team.pdu() = Pdu(district.borough.code, district.borough.description)
