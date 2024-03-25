package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Nsi
import uk.gov.justice.digital.hmpps.entity.NsiType
import java.time.LocalDate

object NSIGenerator {

    val BREACH_TYPE = NsiType(
        "BRE",
        IdGenerator.getAndIncrement(),
    )

    val RECALL_TYPE = NsiType(
        "REC",
        IdGenerator.getAndIncrement()
    )

    val BREACH_NSI = Nsi(
        DetailsGenerator.PERSON.id,
        BREACH_TYPE,
        LocalDate.now(),
        IdGenerator.getAndIncrement()
    )

    val RECALL_NSI = Nsi(
        DetailsGenerator.PERSON.id,
        RECALL_TYPE,
        LocalDate.now(),
        IdGenerator.getAndIncrement()
    )
}
