package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteNomisType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType

object CaseNoteNomisTypeGenerator {
    val DEFAULT = CaseNoteNomisType(
        "NEG IEP_WARN",
        CaseNoteType(
            IdGenerator.getAndIncrement(),
            "CNT1",
            false
        )
    )
}
