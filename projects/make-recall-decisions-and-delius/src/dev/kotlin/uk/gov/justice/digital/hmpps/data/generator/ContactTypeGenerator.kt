package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactType

object ContactTypeGenerator {
    val RECOMMENDATION_STARTED = ContactType(IdGenerator.getAndIncrement(), ContactType.RECOMMENDATION_STARTED)
    val MANAGEMENT_OVERSIGHT_RECALL =
        ContactType(IdGenerator.getAndIncrement(), ContactType.MANAGEMENT_OVERSIGHT_RECALL)
}
