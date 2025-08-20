package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType

object CaseNoteTypeGenerator {
    val DEFAULT = CaseNoteType(IdGenerator.getAndIncrement(), CaseNoteType.DEFAULT_CODE, false)
    val OTHER_INFORMATION = CaseNoteType(IdGenerator.getAndIncrement(), CaseNoteType.OTHER_INFORMATION, false)
}
