package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

object BusinessInteractionGenerator {
    val CASE_NOTES_MERGE = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.CASE_NOTES_MERGE.code,
        ZonedDateTime.now().minusMonths(6)
    )
}
