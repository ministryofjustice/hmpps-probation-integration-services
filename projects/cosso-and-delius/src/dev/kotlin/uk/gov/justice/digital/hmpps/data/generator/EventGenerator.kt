package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.document.Event

object EventGenerator {
    val DEFAULT_EVENT_ID = IdGenerator.getAndIncrement()
    val DEFAULT_EVENT = Event(DEFAULT_EVENT_ID, PersonGenerator.DEFAULT_PERSON.id, "1")
    val MISSING_MAIN_OFFENCE_EVENT = Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON.id, "2")
    val MISSING_COURT_APPEARANCE_EVENT = Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON.id, "3")
    val MISSING_DISPOSAL_EVENT = Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON.id, "4")
    val NO_DOCUMENT_EVENT = Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON.id, "5")
    val TERMINATED_EVENT =
        Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON.id, "6", activeFlag = false)
}