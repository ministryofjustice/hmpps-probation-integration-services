package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider("N03")
    val DEFAULT_TEAM = generateTeam(DEFAULT_PROVIDER.homelessPreventionTeamCode())
    val DEFAULT_STAFF = generateStaff(DEFAULT_TEAM.code + "1", listOf(DEFAULT_TEAM))

    fun generateProvider(code: String, id: Long = IdGenerator.getAndIncrement()) = Provider(code, id)
    fun generateTeam(code: String, id: Long = IdGenerator.getAndIncrement()) = Team(code, id)
    fun generateStaff(code: String, teams: List<Team>, id: Long = IdGenerator.getAndIncrement()) =
        Staff(code, teams, id)
}