package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.District
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamWithDistrict
import java.time.ZonedDateTime

object TeamGenerator {
    val DEFAULT = generate(
        "${ProviderGenerator.DEFAULT.code}UAT",
        "Unallocated Team(N02)",
        ProviderGenerator.DEFAULT.id
    )
    val ALLOCATION_TEAM = generate("N02ABS")
    val TEAM_IN_LAU = generateTeamWithDistrict("N03AAA", "Description for N03AAA", ProviderGenerator.LAU)

    fun generate(
        code: String,
        description: String = code,
        providerId: Long = ProviderGenerator.DEFAULT.id,
        id: Long = IdGenerator.getAndIncrement(),
        endDate: ZonedDateTime? = null,
    ) = Team(id, code, providerId, description, endDate)

    fun generateTeamWithDistrict(
        code: String,
        description: String = code,
        district: District,
        id: Long = IdGenerator.getAndIncrement(),
        endDate: ZonedDateTime? = null,
    ) = TeamWithDistrict(id, code, description, district, endDate)
}
