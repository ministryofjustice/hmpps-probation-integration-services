package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.prepEvent

object MessageGenerator {
    val RECOMMENDATION_STARTED = prepEvent("recommendation-started").message
    val DECISION_TO_RECALL = prepEvent("management-oversight-recall").message
    val DECISION_NOT_TO_RECALL = prepEvent("management-oversight-not-recall").message
}
