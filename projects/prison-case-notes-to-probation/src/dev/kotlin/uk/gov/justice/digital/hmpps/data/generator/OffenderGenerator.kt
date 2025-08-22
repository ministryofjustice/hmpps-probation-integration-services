package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offender

object OffenderGenerator {
    const val EXISTING_OFFENDER_ID = "AA0001A"
    const val NEW_PRISON_IDENTIFIER = "A4578BX"
    val DEFAULT = Offender(IdGenerator.getAndIncrement(), "X123456", EXISTING_OFFENDER_ID)
    val PREVIOUS = Offender(IdGenerator.getAndIncrement(), "P123456", null)
    val NEW_IDENTIFIER = Offender(IdGenerator.getAndIncrement(), "N123456", NEW_PRISON_IDENTIFIER)
}
