package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offender

object OffenderGenerator {
    val DEFAULT = Offender(IdGenerator.getAndIncrement(), "GA52214")
}