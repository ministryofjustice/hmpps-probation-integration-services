package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import java.time.LocalDate

object TeamGenerator {
    val DEFAULT_UPW_TEAM = generateTeam(
        code = "N01UPW",
        description = "N01 UPW Team",
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        upwTeam = true
    )
    val SECOND_UPW_TEAM = generateTeam(
        code = "N01UP2",
        description = "N01 Second UPW Team",
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        upwTeam = true
    )
    val NON_UPW_TEAM = generateTeam(
        code = "N01T99",
        description = "N01 Team 99",
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        upwTeam = false
    )
    val END_DATED_TEAM = generateTeam(
        code = "N01END",
        description = "N01 End Dated Team",
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        upwTeam = true,
        startDate = LocalDate.now().minusDays(2),
        endDate = LocalDate.now().minusDays(1)
    )
    val OTHER_PROVIDER_TEAM = generateTeam(
        code = "N02UP2",
        description = "N02 UPW Team",
        provider = ProviderGenerator.SECOND_PROVIDER,
        upwTeam = true
    )

    fun generateTeam(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        provider: Provider,
        upwTeam: Boolean,
        startDate: LocalDate = LocalDate.now().minusDays(1),
        endDate: LocalDate? = null,
        staff: List<Staff> = emptyList()
    ) = Team(id, code, description, provider, upwTeam, startDate, endDate, staff)
}