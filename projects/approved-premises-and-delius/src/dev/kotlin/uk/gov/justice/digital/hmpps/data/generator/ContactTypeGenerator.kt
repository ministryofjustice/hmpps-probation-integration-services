package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode

object ContactTypeGenerator {
    val APPLICATION_SUBMITTED = generate(ContactTypeCode.APPLICATION_SUBMITTED.code)
    val APPLICATION_ASSESSED = generate(ContactTypeCode.APPLICATION_ASSESSED.code)
    val BOOKING_MADE = generate(ContactTypeCode.BOOKING_MADE.code)
    val ARRIVED = generate(ContactTypeCode.ARRIVED.code)
    val NOT_ARRIVED = generate(ContactTypeCode.NOT_ARRIVED.code)
    val DEPARTED = generate(ContactTypeCode.DEPARTED.code)

    fun generate(
        code: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(id, code)
}
