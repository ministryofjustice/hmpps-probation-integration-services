package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Release
import java.time.LocalDate

object CustodyGenerator {
    val DEFAULT = generate()

    fun generate() = Custody(
        id = id(),
        disposal = DisposalGenerator.DEFAULT,
        releases = listOf(
            Release(
                id = id(),
                date = LocalDate.of(2020, 1, 1)
            ),
            Release(
                id = id(),
                date = LocalDate.of(2010, 1, 1)
            )
        )
    )
}
