package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.audit.BusinessInteraction
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import java.time.ZonedDateTime

object BusinessInteractionGenerator {
    val ALL = BusinessInteractionCode.values().associate {
        it.code to BusinessInteraction(IdGenerator.getAndIncrement(), it.code, ZonedDateTime.now().minusMonths(6))
    }
}
