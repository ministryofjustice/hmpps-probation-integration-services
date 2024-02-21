package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.By
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ManagerIds
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ProviderRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.getByCode

@Service
class ProviderService(
    private val providerRepository: ProviderRepository,
    private val staffRepository: StaffRepository
) {
    fun findManagerIds(by: By): ManagerIds {
        val provider = providerRepository.getByCode(by.probationRegionCode)
        val staff = staffRepository.getByCode(by.staffCode)
        val team = staff.teams.firstOrNull { it.code == provider.homelessPreventionTeamCode() }
            ?: throw IllegalStateException("Staff ${staff.code} not in Team ${provider.homelessPreventionTeamCode()}")
        return object : ManagerIds {
            override val probationAreaId = provider.id
            override val teamId = team.id
            override val staffId = staff.id
        }
    }
}