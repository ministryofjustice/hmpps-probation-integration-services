package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.Team

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider("DEF")
    val DEFAULT_TEAM = generateTeam("DEF1")
    val DEFAULT_STAFF = generateStaff("DEF1S")

    fun generateProvider(code: String, id: Long = IdGenerator.getAndIncrement()) = Provider(code, id)
    fun generateTeam(code: String, id: Long = IdGenerator.getAndIncrement()) = Team(code, id)
    fun generateStaff(code: String, id: Long = IdGenerator.getAndIncrement()) = Staff(code, id)
}