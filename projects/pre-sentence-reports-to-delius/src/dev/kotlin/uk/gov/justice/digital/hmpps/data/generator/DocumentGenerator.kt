package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.courtreport.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.document.Document
import java.time.ZonedDateTime

object DocumentGenerator {
    val DEFAULT = generate(
        CourtReportGenerator.DEFAULT,
        "123-abc",
        "urn:psr-service:pre-sentence-report:f9b09fcf-39c0-4008-8b43-e616ddfd918c",
        "test.pdf"
    )

    fun generate(
        courtReport: CourtReport,
        alfrescoId: String,
        externalReference: String,
        documentName: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Document(
        id,
        courtReport.person.id,
        courtReport.id,
        alfrescoId,
        externalReference,
        name = documentName,
        createdProviderId = 91,
        lastUpdatedProviderId = 91,
        createdByUserId = 23,
        createdDateTime = ZonedDateTime.now(),
        lastSaved = ZonedDateTime.now(),
        lastUpdatedUserId = 23
    )
}
