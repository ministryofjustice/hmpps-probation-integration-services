package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Event

object EventGenerator {
    val DEFAULT_EVENT_ID = IdGenerator.getAndIncrement()
    val DEFAULT_EVENT = Event(DEFAULT_EVENT_ID, PersonGenerator.DEFAULT_PERSON, "1", activeFlag = true, softDeleted = false)
    val MISSING_MAIN_OFFENCE_EVENT =
        Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON, "2", activeFlag = true, softDeleted = false)
    val MISSING_COURT_APPEARANCE_EVENT =
        Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON, "3", activeFlag = true, softDeleted = false)
    val MISSING_DISPOSAL_EVENT =
        Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON, "4", activeFlag = true, softDeleted = false)
    val NO_DOCUMENT_EVENT =
        Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON, "5", activeFlag = true, softDeleted = false)
    val TERMINATED_EVENT =
        Event(IdGenerator.getAndIncrement(), PersonGenerator.DEFAULT_PERSON, "6", activeFlag = false, softDeleted = false)
}