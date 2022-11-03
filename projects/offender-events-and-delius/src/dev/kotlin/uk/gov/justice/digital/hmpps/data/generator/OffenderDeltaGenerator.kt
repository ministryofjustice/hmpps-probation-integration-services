package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.offender.Offender
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDelta
import java.time.ZonedDateTime

object OffenderDeltaGenerator {
    fun generate(
        offender: Offender = OffenderGenerator.DEFAULT,
        sourceTable: String = "OFFENDER",
        sourceId: Long = offender.id,
        action: String = "UPSERT"
    ) = OffenderDelta(
            IdGenerator.getAndIncrement(),
            offender,
            ZonedDateTime.now(),
            action,
            sourceTable,
            sourceId,
            "CREATED",
            ZonedDateTime.now(),
            ZonedDateTime.now()
        )
}