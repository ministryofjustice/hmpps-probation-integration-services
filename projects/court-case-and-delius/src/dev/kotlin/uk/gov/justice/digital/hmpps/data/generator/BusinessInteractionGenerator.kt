package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

object BusinessInteractionGenerator {
    val UPDATE_CONTACT = BusinessInteraction(
        IdGenerator.getAndIncrement(),
        BusinessInteractionCode.UPDATE_CONTACT.code,
        ZonedDateTime.now().minusMonths(6)
    )
}
