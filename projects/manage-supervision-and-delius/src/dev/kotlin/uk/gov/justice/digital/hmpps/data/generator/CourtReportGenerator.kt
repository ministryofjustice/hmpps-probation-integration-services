package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.CourtReportDocument
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.EventDocument
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtReportType
import java.time.ZonedDateTime

object CourtReportGenerator {

    val COURT_APPEARANCE = CourtAppearanceGenerator.generate()
    val DEFAULT_TYPE = CourtReportType(IdGenerator.getAndIncrement(), "Pre-Sentence Report - Fast")
    val COURT_REPORT_TYPE = DEFAULT_TYPE
    val COURT_REPORT = CourtReport(IdGenerator.getAndIncrement(), COURT_REPORT_TYPE, COURT_APPEARANCE)

    val COURT_DOCUMENT = generateCourtDocument(
        PersonGenerator.OVERVIEW.id,
        "A003",
        "court report",
        "DOCUMENT",
        COURT_REPORT.courtReportId
    )

    val EVENT_DOCUMENT = generateEventDocument(
        PersonGenerator.OVERVIEW.id,
        "A004",
        "event report",
        "DOCUMENT",
        PersonGenerator.EVENT_1.id
    )

    fun generateCourtDocument(
        personId: Long,
        alfrescoId: String,
        name: String,
        documentType: String,
        primaryKeyId: Long? = null
    ): CourtReportDocument {
        val doc = CourtReportDocument()
        doc.id = IdGenerator.getAndIncrement()
        doc.lastUpdated = ZonedDateTime.now().minusDays(1)
        doc.alfrescoId = alfrescoId
        doc.name = name
        doc.personId = personId
        doc.primaryKeyId = primaryKeyId
        doc.type = documentType
        return doc
    }

    fun generateEventDocument(
        personId: Long,
        alfrescoId: String,
        name: String,
        documentType: String,
        primaryKeyId: Long? = null
    ): EventDocument {
        val doc = EventDocument()
        doc.id = IdGenerator.getAndIncrement()
        doc.lastUpdated = ZonedDateTime.now().minusDays(3)
        doc.alfrescoId = alfrescoId
        doc.name = name
        doc.personId = personId
        doc.primaryKeyId = primaryKeyId
        doc.type = documentType
        return doc
    }
}