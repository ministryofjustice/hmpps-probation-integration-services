package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offender

object OffenderGenerator {
    const val EXISTING_OFFENDER_ID = "AA0001A"
    const val NEW_PRISON_IDENTIFIER = "A4578BX"
    val DEFAULT = Offender(IdGenerator.getAndIncrement(), EXISTING_OFFENDER_ID)
    val NEW_IDENTIFIER = Offender(IdGenerator.getAndIncrement(), NEW_PRISON_IDENTIFIER)
}
