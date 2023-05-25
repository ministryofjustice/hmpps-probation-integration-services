package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtReportType
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.DocumentCourtAppearance

object CourtReportGenerator {
    val DEFAULT = generate()

    fun generate(
        courtReportType: CourtReportType = CourtReportTypeGenerator.DEFAULT,
        documentCourtAppearance: DocumentCourtAppearance? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtReport(id, documentCourtAppearance, courtReportType)
}
