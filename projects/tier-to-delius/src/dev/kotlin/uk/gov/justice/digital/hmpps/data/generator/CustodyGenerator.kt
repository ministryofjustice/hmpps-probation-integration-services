package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Recall
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Release
import java.time.LocalDate

object CustodyGenerator {
    val DEFAULT = generate(releaseDate = LocalDate.of(2020, 1, 1))
    val RECALLED = generate(
        releaseDate = LocalDate.of(2015, 1, 1),
        recallDate = LocalDate.of(2016, 1, 1),
        disposal = DisposalGenerator.RECALLED,
    )

    fun generate(
        releaseDate: LocalDate,
        recallDate: LocalDate? = null,
        disposal: Disposal = DisposalGenerator.DEFAULT,
    ) = Custody(
        id = id(),
        disposal = disposal,
        releases = listOf(
            Release(
                id = id(),
                date = releaseDate,
                recall = recallDate?.let { Recall(id(), it) }
            ),
            Release(
                id = id(),
                date = LocalDate.of(2010, 1, 1),
                recall = Recall(id(), LocalDate.of(2011, 1, 1))
            )
        )
    )
}
