package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Event

object EventGenerator {
    val DEFAULT_EVENT = Event(id(), "1", PersonGenerator.DEFAULT)
    val NO_DOCUMENT_EVENT = Event(id(), "5", PersonGenerator.DEFAULT)
    val TERMINATED_EVENT = Event(id(), "6", PersonGenerator.DEFAULT)
}