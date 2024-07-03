package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.messaging.ProbationOffenderEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val SENTENCE_CHANGED = ResourceLoader.message<ProbationOffenderEvent>("sentence-changed")
    val SENTENCE_CHANGED_NOT_FOUND = ResourceLoader.message<ProbationOffenderEvent>("sentence-changed-not-found")
    val SENTENCE_CHANGED_MULTIPLE_CUSTODIAL =
        ResourceLoader.message<ProbationOffenderEvent>("sentence-changed-multiple-custodial")
}
