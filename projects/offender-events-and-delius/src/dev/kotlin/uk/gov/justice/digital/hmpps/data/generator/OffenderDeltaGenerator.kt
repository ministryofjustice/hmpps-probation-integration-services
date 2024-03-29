package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.offender.Offender
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDelta
import java.time.ZonedDateTime

object OffenderDeltaGenerator {
    fun generate(
        offender: Offender? = OffenderGenerator.DEFAULT,
        sourceTable: String = "OFFENDER",
        sourceId: Long = offender?.id ?: 0,
        action: String = "UPSERT"
    ) = OffenderDelta(
        IdGenerator.getAndIncrement(),
        offender,
        ZonedDateTime.now(),
        action,
        sourceTable,
        sourceId
    )
}
