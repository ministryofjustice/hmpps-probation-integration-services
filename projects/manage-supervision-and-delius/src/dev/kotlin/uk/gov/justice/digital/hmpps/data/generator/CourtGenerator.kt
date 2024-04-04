package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.Court

object CourtGenerator {
    val DEFAULT = Court(
        IdGenerator.getAndIncrement(),
        "Hull Court"
    )

    val BHAM = Court(
        IdGenerator.getAndIncrement(),
        "Birmingham Court"
    )


}
