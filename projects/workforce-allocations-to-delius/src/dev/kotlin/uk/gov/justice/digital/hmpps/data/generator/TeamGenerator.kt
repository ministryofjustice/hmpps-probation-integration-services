package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team
import java.time.ZonedDateTime

object TeamGenerator {
    val DEFAULT = generate(
        "${ProviderGenerator.DEFAULT.code}UAT",
        "Unallocated Team(N02)",
        ProviderGenerator.DEFAULT.id
    )
    val ALLOCATION_TEAM = generate("N02ABS")

    fun generate(
        code: String,
        description: String = code,
        providerId: Long = ProviderGenerator.DEFAULT.id,
        id: Long = IdGenerator.getAndIncrement(),
        endDate: ZonedDateTime? = null
    ) = Team(id, code, providerId, description, endDate)
}
