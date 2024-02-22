package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.approvedpremesis.By
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@Service
class ProviderService(
    private val providerRepository: ProviderRepository,
    private val staffRepository: StaffRepository,
    private val teamRepository: TeamRepository,
    private val telemetryService: TelemetryService,
) {
    fun findManagerIds(by: By): ManagerIds {
        val provider = providerRepository.getByCode(by.probationRegionCode)
        val staff = staffRepository.getByCode(by.staffCode)
        val team = teamRepository.getByCode(provider.homelessPreventionTeamCode())
        if (staff.teams.none { it.code == provider.homelessPreventionTeamCode() }) {
            telemetryService.trackEvent(
                "StaffNotInTeam",
                mapOf(
                    "staffCode" to staff.code,
                    "reason" to "Officer not in team `${provider.homelessPreventionTeamCode()}`"
                )
            )
        }

        return object : ManagerIds {
            override val probationAreaId = provider.id
            override val teamId = team.id
            override val staffId = staff.id
        }
    }
}