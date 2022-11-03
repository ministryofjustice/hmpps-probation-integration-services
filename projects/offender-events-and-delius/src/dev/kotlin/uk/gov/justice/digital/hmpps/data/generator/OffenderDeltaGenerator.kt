package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDelta
import java.time.ZonedDateTime

object OffenderDeltaGenerator {
    fun generate() = OffenderDelta(
        IdGenerator.getAndIncrement(),
        OffenderGenerator.DEFAULT,
        ZonedDateTime.now(),
        "UPSERT",
        "OFFENDER",
        OffenderGenerator.DEFAULT.id,
        "CREATED",
        ZonedDateTime.now(),
        ZonedDateTime.now()
    )
}