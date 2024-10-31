package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocEvent
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentType
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.EventDocument
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.OffenderDocument
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.ZonedDateTime

object DocumentGenerator {
    val EVENT_DOC = generateEventDoc()
    val PERSON_DOC = generatePersonDoc()

    fun generateEventDoc(
        event: Event = PersonGenerator.ANOTHER_EVENT,
        person: Person = PersonGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement(),
        name: String = "test.doc",
        alfrescoId: String = "uuid1",
        documentType: DocumentType = DocumentType.DOCUMENT
    ): EventDocument {
        val doc = EventDocument(event.toDocEvent())
        doc.id = id
        doc.person = person
        doc.name = name
        doc.primaryKeyId = doc.event?.id!!
        doc.alfrescoId = alfrescoId
        doc.lastSaved = ZonedDateTime.now().minusDays(7)
        doc.dateProduced = null
        doc.type = documentType
        return doc
    }

    fun generatePersonDoc(
        id: Long = IdGenerator.getAndIncrement(),
        person: Person = PersonGenerator.DEFAULT,
        name: String = "offender.doc",
        alfrescoId: String = "uuid2",
        documentType: DocumentType = DocumentType.DOCUMENT
    ): OffenderDocument {
        val doc = OffenderDocument()
        doc.id = id
        doc.person = person
        doc.name = name
        doc.primaryKeyId = person.id
        doc.alfrescoId = alfrescoId
        doc.lastSaved = ZonedDateTime.now().minusDays(7)
        doc.dateProduced = null
        doc.type = documentType

        return doc
    }

    private fun Event.toDocEvent() =
        DocEvent(id, Person(personId, "", false), true, number, null, null)
}
