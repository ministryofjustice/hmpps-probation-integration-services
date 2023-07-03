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
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class ManagerService(
    private val personManagerRepository: PersonManagerRepository,
    private val prisonManagerRepository: PrisonManagerRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun findResponsibleCommunityManager(crn: String): ResponsibleOfficer {
        val com = personManagerRepository.findByPersonCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        com.staff.user?.apply {
            email = ldapTemplate.findEmailByUsername(username)
        }
        val pom = if (com.responsibleOfficer == null) prisonManagerRepository.findByPersonId(com.person.id) else null
        return com.toResponsibleOfficer(pom)
    }

    fun findCasesManagedBy(username: String) = ManagedCases(
        (personManagerRepository.findCasesManagedBy(username) + personManagerRepository.findCasesManagedBy(username)).toSet()
            .map { CaseIdentifier(it) }
    )
}

fun PersonManager.toResponsibleOfficer(pom: PrisonManager?) = ResponsibleOfficer(
    Manager(staff.code, staff.name(), staff.user?.username, staff.user?.email, responsibleOfficer != null, team.pdu()),
    pom?.toManager()
)

fun PrisonManager.toManager() =
    Manager(staff.code, staff.name(), null, emailAddress, responsibleOfficer != null, team.pdu())

fun Staff.name() = Name(forename, surname)

fun Team.pdu() = Pdu(district.borough.code, district.borough.description)
