package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import uk.gov.justice.digital.hmpps.entity.event.EventEntity
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_EXPIRED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_REVIEWED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.CHECK_IN_UPDATED
import uk.gov.justice.digital.hmpps.messaging.Handler.Companion.SETUP_COMPLETED
import java.time.LocalDate
import java.time.ZonedDateTime

object ContactGenerator {
    val CONTACT_TO_REVIEW =
        generateContact(externalReference = Contact.externalReferencePrefix(CHECK_IN_REVIEWED) + "8b8a8cf1-a8fe-42c4-879c-095bbed91466")
    val CONTACT_TO_UPDATE =
        generateContact(externalReference = Contact.externalReferencePrefix(CHECK_IN_UPDATED) + "a18648f4-46ec-4344-8e8e-ba15c18c3ab9")
    val CONTACT_TO_UPDATE_EXPIRY =
        generateContact(externalReference = Contact.externalReferencePrefix(CHECK_IN_EXPIRED) + "b5a4d4c6-15c5-4f54-8ec2-f7f38c6f8b23")
    val SETUP_CONTACT_TO_UPDATE = generateContact(
        type = ContactTypeGenerator.CT_ESPCHS,
        outcome = ContactOutcomeGenerator.COT_ESPC,
        externalReference = Contact.externalReferencePrefix(SETUP_COMPLETED) + "5b487c04-974d-44ca-b8c2-c95053d82479"
    )
    val SETUP_CONTACT_NULL_EVENT = generateContact(
        event = null,
        type = ContactTypeGenerator.CT_ESPCHS,
        outcome = ContactOutcomeGenerator.COT_ESPC,
        externalReference = Contact.externalReferencePrefix(SETUP_COMPLETED) + "d9e1f2a3-b4c5-6789-0abc-def123456789"
    )
    val SENTENCE_TERMINATED_SETUP_CONTACT = generateContact(
        person = PersonGenerator.SENTENCE_TERMINATED_PERSON,
        event = EventGenerator.SENTENCE_TERMINATED_EVENT,
        type = ContactTypeGenerator.CT_ESPCHS,
        outcome = ContactOutcomeGenerator.COT_ESPC,
        externalReference = Contact.externalReferencePrefix(SETUP_COMPLETED) + "8f692c7f-8047-49c3-bb53-8572aefee012"
    )

    fun generateContact(
        person: Person = PersonGenerator.DEFAULT_PERSON,
        event: EventEntity? = EventGenerator.EVENT_2,
        type: ContactType = ContactTypeGenerator.CT_ESPCHI,
        outcome: ContactOutcome? = null,
        date: LocalDate = LocalDate.now(),
        startTime: ZonedDateTime = ZonedDateTime.now(),
        provider: Provider = ProviderGenerator.DEFAULT_PROVIDER,
        team: Team = ProviderGenerator.DEFAULT_TEAM,
        staff: Staff = ProviderGenerator.DEFAULT_STAFF,
        description: String = "Contact Description",
        notes: String? = "Existing Notes",
        externalReference: String? = null,
        softDeleted: Boolean = false,
        id: Long = 0,
        isSensitive: Boolean = false,
    ) = Contact(
        person = person,
        event = event,
        type = type,
        outcome = outcome,
        date = date,
        startTime = startTime,
        provider = provider,
        team = team,
        staff = staff,
        description = description,
        notes = notes,
        externalReference = externalReference,
        softDeleted = softDeleted,
        id = id,
        isSensitive = isSensitive
    )
}
