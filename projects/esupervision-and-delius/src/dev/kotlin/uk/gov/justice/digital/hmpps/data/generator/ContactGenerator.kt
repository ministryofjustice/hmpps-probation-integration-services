package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.*
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_REVIEWED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_UPDATED
import java.time.LocalDate
import java.time.ZonedDateTime

object ContactGenerator {
    val CONTACT_TO_REVIEW =
        generateContact(externalReference = Contact.externalReferencePrefix(CHECK_IN_REVIEWED) + "8b8a8cf1-a8fe-42c4-879c-095bbed91466")
    val CONTACT_TO_UPDATE =
        generateContact(externalReference = Contact.externalReferencePrefix(CHECK_IN_UPDATED) + "a18648f4-46ec-4344-8e8e-ba15c18c3ab9")

    fun generateContact(
        person: Person = PersonGenerator.DEFAULT_PERSON,
        event: Event = PersonGenerator.DEFAULT_EVENT,
        contactType: ContactType = ContactTypeGenerator.CT_ESPCHI,
        date: LocalDate = LocalDate.now(),
        startTime: ZonedDateTime = ZonedDateTime.now(),
        provider: Provider = ProviderGenerator.DEFAULT_PROVIDER,
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = ProviderGenerator.DEFAULT_STAFF,
        description: String = "Contact Description",
        notes: String? = "Existing Notes",
        externalReference: String? = null,
        softDeleted: Boolean = false,
        id: Long = 0
    ) = Contact(
        person = person,
        event = event,
        type = contactType,
        date = date,
        startTime = startTime,
        provider = provider,
        team = team,
        staff = staff,
        description = description,
        notes = notes,
        externalReference = externalReference,
        softDeleted = softDeleted,
        id = id
    )
}