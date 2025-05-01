package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

object ContactGenerator {
    val TYPES = ContactType.Code.entries.map { generateType(it.value) }.associateBy { it.code }

    fun generateContact(
        person: Person,
        type: ContactType,
        eventId: Long? = null,
        externalReference: String? = null,
        alert: Boolean? = false,
        softDeleted: Boolean = false
    ) = Contact(type, person, eventId, externalReference, alert, softDeleted)

    fun generateType(code: String, id: Long = IdGenerator.getAndIncrement()) = ContactType(code, id)
}