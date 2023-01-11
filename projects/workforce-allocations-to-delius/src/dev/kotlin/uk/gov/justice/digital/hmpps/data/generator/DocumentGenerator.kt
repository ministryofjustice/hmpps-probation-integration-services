package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtReportDocument
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocEvent
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentType
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.EventDocument
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.OffenderDocument
import uk.gov.justice.digital.hmpps.integrations.delius.event.Event
import java.time.ZonedDateTime
import java.util.UUID

object DocumentGenerator {
    val COURT_REPORT = generateCourtReportDoc()
    val PREVIOUS_CONVICTION = generatePreConDoc()
    val CPS_PACK = generateCpsPackDoc()

    fun generateCourtReportDoc(
        personId: Long = PersonGenerator.DEFAULT.id,
        courtReport: CourtReport = CourtReportGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ): CourtReportDocument {
        val doc = CourtReportDocument(courtReport)
        doc.id = id
        doc.personId = personId
        doc.name = "CourtReport.doc"
        doc.primaryKeyId = courtReport.id
        doc.alfrescoId = UUID.randomUUID().toString()
        doc.dateProduced = ZonedDateTime.now().minusDays(5)
        return doc
    }

    fun generatePreConDoc(
        personId: Long = PersonGenerator.DEFAULT.id,
        id: Long = IdGenerator.getAndIncrement()
    ): OffenderDocument {
        val doc = OffenderDocument()
        doc.id = id
        doc.personId = personId
        doc.name = "PreviousConviction.doc"
        doc.primaryKeyId = personId
        doc.alfrescoId = UUID.randomUUID().toString()
        doc.dateProduced = ZonedDateTime.now().minusDays(30)
        doc.type = DocumentType.PREVIOUS_CONVICTION
        return doc
    }

    fun generateCpsPackDoc(
        personId: Long = PersonGenerator.DEFAULT.id,
        event: Event = EventGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ): EventDocument {
        val doc = EventDocument(event.toDocEvent())
        doc.id = id
        doc.personId = personId
        doc.name = "CpsPack.doc"
        doc.primaryKeyId = doc.event?.id!!
        doc.alfrescoId = UUID.randomUUID().toString()
        doc.lastSaved = ZonedDateTime.now().minusDays(7)
        doc.dateProduced = null
        doc.type = DocumentType.CPS_PACK
        return doc
    }

    private fun Event.toDocEvent() = DocEvent(id, person, true, number, null, null)
}
