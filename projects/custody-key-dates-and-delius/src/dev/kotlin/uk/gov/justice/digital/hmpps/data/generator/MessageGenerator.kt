package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.messaging.CustodyDateChanged
import uk.gov.justice.digital.hmpps.messaging.ProbationOffenderEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val SENTENCE_DATE_CHANGED = ResourceLoader.message<CustodyDateChanged>("sentence-date-changed")
    val SENTENCE_CHANGED = ResourceLoader.message<ProbationOffenderEvent>("sentence-changed")
}
