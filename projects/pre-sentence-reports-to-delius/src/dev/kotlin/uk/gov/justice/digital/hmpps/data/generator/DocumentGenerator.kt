package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.document.Document

object DocumentGenerator {
    val DEFAULT = generate(CourtReportGenerator.DEFAULT.id, "123-abc", "urn:psr-service:pre-sentence-report:f9b09fcf-39c0-4008-8b43-e616ddfd918c", "test.pdf")

    fun generate(
        courtReportId: Long,
        alfrescoId: String,
        externalReference: String,
        documentName: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Document(id, courtReportId, alfrescoId, externalReference, documentName)
}
