package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Offender
import java.time.ZonedDateTime

object NsiGenerator {

    val DEFAULT_EVENT = Event(IdGenerator.getAndIncrement(), "1")

    val DEFAULT_NSI = Nsi(
        2500000986,
        ZonedDateTime.parse("2022-01-31T00:00:00+00:00"),
        Offender(IdGenerator.getAndIncrement(), "D006926"),
        DEFAULT_EVENT,
    )
}
