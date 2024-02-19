package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.courtappearance.Court

object CourtGenerator {
    val DEFAULT = Court(
        IdGenerator.getAndIncrement(),
        "Hull Court"
    )
}
