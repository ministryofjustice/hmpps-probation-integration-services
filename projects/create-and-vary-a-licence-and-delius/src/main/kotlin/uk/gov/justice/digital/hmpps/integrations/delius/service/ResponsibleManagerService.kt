package uk.gov.justice.digital.hmpps.integrations.delius.service

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

@Service
class ResponsibleManagerService(private val responsibleOfficerRepository: ResponsibleOfficerRepository) {
    fun findResponsibleCommunityManager(crn: String): Manager? =
        responsibleOfficerRepository.findResponsibleOfficer(crn)?.asManager()
}

fun ResponsibleOfficer.asManager() = Manager(
    communityManager.staff.code,
    communityManager.staff.name(),
    communityManager.provider.asProvider(),
    communityManager.team.asTeam()
)

fun Staff.name() = Name(forename, middleName, surname)
fun Provider.asProvider() = uk.gov.justice.digital.hmpps.api.model.Provider(code, description)
fun Team.asTeam() = uk.gov.justice.digital.hmpps.api.model.Team(code, description, district.asDistrict())
fun District.asDistrict() = uk.gov.justice.digital.hmpps.api.model.District(code, description, borough.asBorough())
fun Borough.asBorough() = uk.gov.justice.digital.hmpps.api.model.Borough(code, description)
