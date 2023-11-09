package uk.gov.justice.digital.hmpps.integrations.delius.manager

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.Manager
import uk.gov.justice.digital.hmpps.api.Name
import uk.gov.justice.digital.hmpps.api.Team
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.CommunityManager
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.CommunityManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.getByNomsId

@Service
class ManagerService(private val communityManagerRepository: CommunityManagerRepository) {
    fun findCommunityManager(nomsId: String) = communityManagerRepository.getByNomsId(nomsId).asManager()
}

fun CommunityManager.asManager() = Manager(staff.code, staff.name(), Team(team.code, team.description))
fun Staff.name() = Name(forename, surname)
