package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.contact.entity.ContactType

object ContactTypeGenerator {
    val RECOMMENDATION_STARTED = ContactType(IdGenerator.getAndIncrement(), "MRD01")
}
