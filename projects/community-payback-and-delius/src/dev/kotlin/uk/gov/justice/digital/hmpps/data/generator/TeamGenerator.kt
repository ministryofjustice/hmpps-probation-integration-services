package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.*
import java.time.LocalDate

object TeamGenerator {
    val DEFAULT_UPW_TEAM = generateTeam(
        code = "N01UPW",
        description = "N01 UPW Team",
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        localAdminUnit = LauGenerator.DEFAULT_PROVIDER_LOCAL_ADMIN_UNIT,
        upwTeam = true
    )
    val SECOND_UPW_TEAM = generateTeam(
        code = "N01UP2",
        description = "N01 Second UPW Team",
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        localAdminUnit = LauGenerator.DEFAULT_PROVIDER_LOCAL_ADMIN_UNIT,
        upwTeam = true
    )
    val NON_UPW_TEAM = generateTeam(
        code = "N01T99",
        description = "N01 Team 99",
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        localAdminUnit = LauGenerator.DEFAULT_PROVIDER_LOCAL_ADMIN_UNIT,
        upwTeam = false
    )
    val END_DATED_TEAM = generateTeam(
        code = "N01END",
        description = "N01 End Dated Team",
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        localAdminUnit = LauGenerator.DEFAULT_PROVIDER_LOCAL_ADMIN_UNIT,
        upwTeam = true,
        startDate = LocalDate.now().minusDays(2),
        endDate = LocalDate.now().minusDays(1)
    )
    val OTHER_PROVIDER_TEAM = generateTeam(
        code = "N02UP2",
        description = "N02 UPW Team",
        provider = ProviderGenerator.SECOND_PROVIDER,
        localAdminUnit = LauGenerator.SECOND_PROVIDER_LOCAL_ADMIN_UNIT,
        upwTeam = true
    )

    val DEFAULT_TEAM_PICKUP_LOCATION by lazy {
        TeamOfficeLocation(
            team = OTHER_PROVIDER_TEAM,
            officeLocation = UPWGenerator.SECOND_PROVIDER_OFFICE_LOCATION,
        )
    }

    fun generateTeam(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        provider: Provider,
        localAdminUnit: LocalAdminUnit? = null,
        upwTeam: Boolean,
        startDate: LocalDate = LocalDate.now().minusDays(1),
        endDate: LocalDate? = null,
        staff: List<Staff> = emptyList(),
        unallocatedStaff: List<Staff> = emptyList(),
    ) = Team(
        id,
        code,
        description,
        provider,
        upwTeam,
        startDate,
        endDate,
        staff,
        unallocatedStaff,
        localAdminUnit = localAdminUnit
    )
}