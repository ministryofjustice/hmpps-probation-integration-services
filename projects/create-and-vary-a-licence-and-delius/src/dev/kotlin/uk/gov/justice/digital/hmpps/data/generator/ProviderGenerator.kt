package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Borough
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.LocalDate

object ProviderGenerator {
    val DEFAULT_PROVIDER = generateProvider("N01")
    val DEFAULT_BOROUGH = generateBorough("N01B")
    val DEFAULT_DISTRICT = generateDistrict("N01D")
    val DEFAULT_TEAM = generateTeam("N01BDT")

    fun generateProvider(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement(),
        endDate: LocalDate? = null
    ) = Provider(code, description, id, endDate)

    fun generateBorough(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement(),
        pduHeads: List<Staff> = listOf(StaffGenerator.DEFAULT)
    ) = Borough(code, description, id, pduHeads, DEFAULT_PROVIDER)

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
