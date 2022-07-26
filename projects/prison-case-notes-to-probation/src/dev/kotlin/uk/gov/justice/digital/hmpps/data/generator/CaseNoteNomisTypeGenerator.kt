package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteNomisType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType

object CaseNoteNomisTypeGenerator {
    val AREL = CaseNoteNomisType(
        "AR EVENT-LEVEL CASENOTE",
        CaseNoteType(
            IdGenerator.getAndIncrement(),
            "AR01",
            false
        )
    )
}
