package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.document.Event

object EventGenerator {
    val DEFAULT_EVENT_ID = IdGenerator.getAndIncrement()
    val DEFAULT_EVENT = Event(DEFAULT_EVENT_ID, "1")
    val MISSING_MAIN_OFFENCE_EVENT = Event(IdGenerator.getAndIncrement(), "2")
    val MISSING_COURT_APPEARANCE_EVENT = Event(IdGenerator.getAndIncrement(), "3")
    val MISSING_DISPOSAL_EVENT = Event(IdGenerator.getAndIncrement(), "4")
}