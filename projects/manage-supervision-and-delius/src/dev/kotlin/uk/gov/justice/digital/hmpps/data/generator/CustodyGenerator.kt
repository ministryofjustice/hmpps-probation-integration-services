package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ACTIVE_ORDER
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Release
import java.time.ZonedDateTime

object CustodyGenerator {
    val CUSTODY_1 = Custody(IdGenerator.getAndIncrement(), ACTIVE_ORDER.id, ACTIVE_ORDER, listOf(), false)

    val RELEASE_1 = Release(
        IdGenerator.getAndIncrement(),
        CUSTODY_1,
        ZonedDateTime.now().minusDays(21),
        ZonedDateTime.now().minusDays(28),
        false
    )

    val RELEASE_2 = Release(
        IdGenerator.getAndIncrement(),
        CUSTODY_1,
        ZonedDateTime.now().minusDays(14),
        ZonedDateTime.now().minusDays(21),
        false
    )

    val RELEASE_3 = Release(
        IdGenerator.getAndIncrement(),
        CUSTODY_1,
        ZonedDateTime.now().minusDays(7),
        ZonedDateTime.now().minusDays(14),
        false
    )
}