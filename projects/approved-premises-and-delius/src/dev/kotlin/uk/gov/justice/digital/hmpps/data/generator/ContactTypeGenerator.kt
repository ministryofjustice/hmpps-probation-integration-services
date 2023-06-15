package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactType

object ContactTypeGenerator {
    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(id, code)
}

object ContactOutcomeGenerator {
    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactOutcome(id, code)
}
