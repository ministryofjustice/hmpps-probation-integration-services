package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider("N01")
    val DEFAULT_BOROUGH = generateBorough("N01B")
    val DEFAULT_DISTRICT = generateDistrict("N01D")
    val DEFAULT_TEAM = generateTeam("N01BDT")

    fun generateProvider(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Provider(code, description, id)

    fun generateBorough(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = Borough(code, description, id)

    fun generateDistrict(
        code: String,
        description: String = "Description of $code",
        borough: Borough = DEFAULT_BOROUGH,
        id: Long = IdGenerator.getAndIncrement()
    ) = District(code, description, borough, id)

    fun generateTeam(
        code: String,
        description: String = "Description of $code",
        district: District = DEFAULT_DISTRICT,
        id: Long = IdGenerator.getAndIncrement()
    ) = Team(code, description, district, id)
}
