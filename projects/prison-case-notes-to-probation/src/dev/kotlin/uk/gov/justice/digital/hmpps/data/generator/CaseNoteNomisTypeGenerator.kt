package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteNomisType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType

object CaseNoteNomisTypeGenerator {
    val NEG = CaseNoteNomisType(
        "NEG IEP_WARN",
        CaseNoteType(
            IdGenerator.getAndIncrement(),
            "CNT1",
            false
        )
    )
    val ALERT = CaseNoteNomisType(
        "ALERT ACTIVE",
        CaseNoteType(
            IdGenerator.getAndIncrement(),
            "CNT2",
            false
        )
    )
}
