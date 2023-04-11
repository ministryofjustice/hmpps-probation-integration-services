package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.ldap.LdapService

@Service
class ManagerService(
    private val personManagerRepository: PersonManagerRepository,
    private val ldapService: LdapService
) {
    fun findResponsibleCommunityManager(crn: String): ResponsibleOfficer {
        val com = personManagerRepository.findByPersonCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
        com.staff.user?.apply {
            email = ldapService.findEmailByUsername(username)
        }
        return com.toResponsibleOfficer()
    }
}

fun PersonManager.toResponsibleOfficer() = ResponsibleOfficer(
    Manager(staff.code, staff.name(), staff.user?.username, staff.user?.email, responsibleOfficer != null)
)

fun Staff.name() = Name(forename, surname)
