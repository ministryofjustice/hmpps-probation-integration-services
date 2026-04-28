package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactDocument
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.Document
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.PersonDocument
import java.time.ZonedDateTime

object DocumentGenerator {
    val DEFAULT_DOCUMENT_FOR_CONTACT = generateDocument(
        ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT,
        ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.person.id,
        "0000-0000-0000-0000-0000",
        "TestDocument.doc",
        "CONTACT",
        ContactGenerator.PREVIOUS_APPT_CONTACT_ABSENT.id
    )


    fun generateDocument(
        contact: Contact,
        personId: Long,
        alfrescoId: String,
        name: String,
        documentType: String,
        primaryKeyId: Long? = null
    ): ContactDocument {
        val doc = ContactDocument(contact)
        doc.createdAt = ZonedDateTime.now().minusDays(2)
        doc.lastUpdated = ZonedDateTime.now().minusDays(1)
        doc.alfrescoId = alfrescoId
        doc.name = name
        doc.personId = personId
        doc.primaryKeyId = primaryKeyId
        doc.type = documentType
        return doc
    }
}