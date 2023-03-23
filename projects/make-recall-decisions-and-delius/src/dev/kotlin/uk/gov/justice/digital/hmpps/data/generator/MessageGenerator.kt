package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val RECOMMENDATION_STARTED = ResourceLoader.event("recommendation-started")
    val DECISION_TO_RECALL = ResourceLoader.event("management-oversight-recall")
    val DECISION_NOT_TO_RECALL = ResourceLoader.event("management-oversight-not-recall")
}
