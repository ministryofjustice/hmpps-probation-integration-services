package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

object BusinessInteractionGenerator {
    val UPLOAD_DOCUMENT = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.UPLOAD_DOCUMENT.code,
        ZonedDateTime.now().minusMonths(6)
    )
}
