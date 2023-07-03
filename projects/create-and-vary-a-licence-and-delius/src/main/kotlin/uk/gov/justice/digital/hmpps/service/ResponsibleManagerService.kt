package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class ResponsibleManagerService(
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun findResponsibleCommunityManager(crn: String): Manager? =
        responsibleOfficerRepository.findResponsibleOfficer(crn)?.let { ro ->
            ro.communityManager.staff.user?.apply {
                email = ldapTemplate.findEmailByUsername(username)
            }
            ro.asManager()
        }
}

fun ResponsibleOfficer.asManager() = Manager(
    communityManager.staff.code,
    communityManager.staff.name(),
    communityManager.provider.asProvider(),
    communityManager.team.asTeam(),
    communityManager.staff.user?.username,
    communityManager.staff.user?.email
)

fun Staff.name() = Name(forename, middleName, surname)
fun Provider.asProvider() = uk.gov.justice.digital.hmpps.api.model.Provider(code, description)
fun Team.asTeam() = uk.gov.justice.digital.hmpps.api.model.Team(code, description, district.asDistrict())
fun District.asDistrict() = uk.gov.justice.digital.hmpps.api.model.District(code, description, borough.asBorough())
fun Borough.asBorough() = uk.gov.justice.digital.hmpps.api.model.Borough(code, description)
