package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.document.Nsi

object NsiGenerator {
    val DEFAULT_NSI = Nsi(
        nsiId = IdGenerator.getAndIncrement(),
        eventId = EventGenerator.DEFAULT_EVENT.id,
    )
}

