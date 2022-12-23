package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val RECOMMENDATION_STARTED = ResourceLoader.message<HmppsDomainEvent>("recommendation-started")
    val DECISION_TO_RECALL = ResourceLoader.message<HmppsDomainEvent>("management-oversight-recall")
    val DECISION_NOT_TO_RECALL = ResourceLoader.message<HmppsDomainEvent>("management-oversight-not-recall")
}
