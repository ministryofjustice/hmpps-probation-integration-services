package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator.DEFAULT_COURT_REPORT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT_PERSON
import uk.gov.justice.digital.hmpps.integrations.delius.Document
import java.util.*

object DocumentGenerator {
    val DOCUMENT_UUID = UUID.randomUUID().toString()
    val DOCUMENT_INVALID_UUID = UUID.randomUUID().toString()

    val DEFAULT_DOCUMENT = Document(
        DEFAULT_PERSON, DEFAULT_COURT_REPORT,
        "COURT_REPORT", "urn:hmpps:pre-sentence-service:report:$DOCUMENT_UUID", 0, false, IdGenerator.getAndIncrement()
    )
}