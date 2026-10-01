package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.LocalAdminUnitGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationDeliveryUnitGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffTeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.loader.BaseDataLoader
import uk.gov.justice.digital.hmpps.data.manager.DataManager

@Component
class DataLoader(dataManager: DataManager) : BaseDataLoader(dataManager) {
    override fun systemUser() = UserGenerator.AUDIT_USER

    override fun setupData() {
        saveAll(
            ProviderGenerator.DEFAULT,
            ProviderGenerator.LONDON,
            ProviderGenerator.NORTH,
            ProviderGenerator.INACTIVE
        )

        saveAll(
            ProbationDeliveryUnitGenerator.DEFAULT,
            ProbationDeliveryUnitGenerator.SECOND,
            ProbationDeliveryUnitGenerator.THIRD,
            ProbationDeliveryUnitGenerator.INACTIVE_PDU,
            ProbationDeliveryUnitGenerator.PDU_IN_INACTIVE_PROVIDER,
            ProbationDeliveryUnitGenerator.LONDON_PDU
        )

        saveAll(
            LocalAdminUnitGenerator.DEFAULT,
            LocalAdminUnitGenerator.SECOND,
            LocalAdminUnitGenerator.THIRD,
            LocalAdminUnitGenerator.LONDON,
            LocalAdminUnitGenerator.INACTIVE
        )

        saveAll(
            TeamGenerator.DEFAULT,
            TeamGenerator.SECOND,
            TeamGenerator.THIRD,
            TeamGenerator.LONDON_TEAM
        )

        saveAll(
            UserGenerator.DEFAULT_USER,
            UserGenerator.LONDON_USER,
            UserGenerator.MULTI_TEAM_USER
        )

        saveAll(
            StaffTeamGenerator.DEFAULT_USER_TEAM_1,
            StaffTeamGenerator.DEFAULT_USER_TEAM_2,
            StaffTeamGenerator.LONDON_USER_TEAM,
            StaffTeamGenerator.MULTI_TEAM_USER_TEAM_1,
            StaffTeamGenerator.MULTI_TEAM_USER_TEAM_2,
            StaffTeamGenerator.MULTI_TEAM_USER_TEAM_3
        )
    }
}


