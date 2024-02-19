package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offender

object OffenderGenerator {
    const val EXISTING_OFFENDER_ID = "AA0001A"
    val DEFAULT = Offender(IdGenerator.getAndIncrement(), EXISTING_OFFENDER_ID)
}
