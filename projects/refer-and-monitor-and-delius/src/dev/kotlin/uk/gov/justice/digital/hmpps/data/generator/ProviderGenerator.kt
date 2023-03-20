package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team

object ProviderGenerator {
    val INTENDED_PROVIDER = generateProvider(Provider.INTENDED_PROVIDER_CODE)
    val INTENDED_TEAM = generateTeam(Team.INTENDED_TEAM_CODE)
    val INTENDED_STAFF = generateStaff(Staff.INTENDED_STAFF_CODE)

    fun generateProvider(code: String, id: Long = IdGenerator.getAndIncrement()) = Provider(code, id)
    fun generateTeam(code: String, id: Long = IdGenerator.getAndIncrement()) = Team(code, id)
    fun generateStaff(code: String, id: Long = IdGenerator.getAndIncrement()) = Staff(code, id)
}
