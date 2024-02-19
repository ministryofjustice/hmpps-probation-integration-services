package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Nsi
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

object NSIGenerator {

    val BREACH_RD = ReferenceData(
        IdGenerator.getAndIncrement(),
        "BRE01",
        "Breach"
    )

    val RECALL_RD = ReferenceData(
        IdGenerator.getAndIncrement(),
        "REC01",
        "Recall"
    )

    val BREACH_NSI = Nsi(
        DetailsGenerator.PERSON.id,
        BREACH_RD,
        LocalDate.now(),
        IdGenerator.getAndIncrement()
    )

    val RECALL_NSI = Nsi(
        DetailsGenerator.PERSON.id,
        RECALL_RD,
        LocalDate.now(),
        IdGenerator.getAndIncrement()
    )
}
