package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.prepEvent

object MessageGenerator {
    val DECISION_TO_RECALL = prepEvent("management-oversight-recall").message
    val DECISION_NOT_TO_RECALL = prepEvent("management-oversight-not-recall").message
}
