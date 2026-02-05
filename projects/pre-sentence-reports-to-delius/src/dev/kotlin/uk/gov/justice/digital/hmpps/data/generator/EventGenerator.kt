package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Event

object EventGenerator {
    val DEFAULT_EVENT = Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON.id, 1L)
}