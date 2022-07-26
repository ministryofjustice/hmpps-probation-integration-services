package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.NomisTypeNsiType

object NomisNsiTypeGenerator {
    val DEFAULT = NomisTypeNsiType(
        IdGenerator.getAndIncrement(),
        CaseNoteNomisTypeGenerator.AREL.nomisCode,
        NsiGenerator.EVENT_CASE_NOTE_NSI.type
    )
}
