package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.offender.Offender

object OffenderGenerator {
    val DEFAULT = Offender(IdGenerator.getAndIncrement(), "X123456", "A81026Y")
}
