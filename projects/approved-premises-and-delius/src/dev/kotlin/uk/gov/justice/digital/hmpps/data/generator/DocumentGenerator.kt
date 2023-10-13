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
        id: Long = IdGenerator.getAndIncrement()
    ): EventDocument {
        val doc = EventDocument(event.toDocEvent())
        doc.id = id
        doc.person = PersonGenerator.DEFAULT
        doc.name = "test.doc"
        doc.primaryKeyId = doc.event?.id!!
        doc.alfrescoId = "uuid1"
        doc.lastSaved = ZonedDateTime.now().minusDays(7)
        doc.dateProduced = null
        doc.type = DocumentType.DOCUMENT
        return doc
    }

    fun generatePersonDoc(
        id: Long = IdGenerator.getAndIncrement()
    ): OffenderDocument {
        val doc = OffenderDocument()
        doc.id = id
        doc.person = PersonGenerator.DEFAULT
        doc.name = "offender.doc"
        doc.primaryKeyId = PersonGenerator.DEFAULT.id
        doc.alfrescoId = "uuid2"
        doc.lastSaved = ZonedDateTime.now().minusDays(7)
        doc.dateProduced = null
        doc.type = DocumentType.DOCUMENT

        return doc
    }

    private fun Event.toDocEvent() =
        DocEvent(id, Person(personId, "", false), true, number, null, null)
}
