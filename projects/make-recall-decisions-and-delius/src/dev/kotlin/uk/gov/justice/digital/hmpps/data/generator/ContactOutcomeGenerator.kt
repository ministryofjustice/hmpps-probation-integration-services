package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactOutcome

object ContactOutcomeGenerator {
    val DECISION_TO_RECALL = ContactOutcome(IdGenerator.getAndIncrement(), ContactOutcome.DECISION_TO_RECALL)
    val DECISION_NOT_TO_RECALL = ContactOutcome(IdGenerator.getAndIncrement(), ContactOutcome.DECISION_NOT_TO_RECALL)
}
