package uk.gov.justice.digital.hmpps.messaging

import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactOutcome

enum class ManagementDecision(val code: String) {
    DECISION_TO_RECALL(ContactOutcome.DECISION_TO_RECALL),
    DECISION_NOT_TO_RECALL(ContactOutcome.DECISION_NOT_TO_RECALL),
}
