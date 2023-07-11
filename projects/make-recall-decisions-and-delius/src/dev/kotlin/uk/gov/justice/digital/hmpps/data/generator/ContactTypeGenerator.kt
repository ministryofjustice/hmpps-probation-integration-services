package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactType

object ContactTypeGenerator {
    val MANAGEMENT_OVERSIGHT_RECALL =
        ContactType(IdGenerator.getAndIncrement(), ContactType.MANAGEMENT_OVERSIGHT_RECALL)
}
