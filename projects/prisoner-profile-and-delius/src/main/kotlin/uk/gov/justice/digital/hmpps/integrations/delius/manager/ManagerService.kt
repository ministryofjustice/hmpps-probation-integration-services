package uk.gov.justice.digital.hmpps.integrations.delius.manager

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.Manager
import uk.gov.justice.digital.hmpps.api.Name
import uk.gov.justice.digital.hmpps.api.Team
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.CommunityManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.getByNomsId
import uk.gov.justice.digital.hmpps.integrations.ldap.entity.LdapUserDetails
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class ManagerService(
    private val communityManagerRepository: CommunityManagerRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun findCommunityManager(nomsId: String) = communityManagerRepository.getByNomsId(nomsId).apply {
        staff.user?.let {
            ldapTemplate.findByUsername<LdapUserDetails>(it.username)?.apply {
                it.email = email
                it.telephone = telephone
            }
        }
    }.asManager()
}

fun CommunityManager.asManager() = Manager(staff.code, staff.name(), team(), staff.user?.email, staff.user?.telephone)
fun Staff.name() = Name(forename, surname)
fun CommunityManager.team() = Team(team.code, team.description, team.email, team.telephone)
