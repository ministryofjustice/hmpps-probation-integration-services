package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offender

object OffenderGenerator {
    val DEFAULT = Offender(IdGenerator.getAndIncrement(), CaseNoteMessageGenerator.EXISTS_IN_DELIUS.offenderId)
}