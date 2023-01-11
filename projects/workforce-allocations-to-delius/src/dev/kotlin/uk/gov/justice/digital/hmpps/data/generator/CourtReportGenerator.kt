package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtReportType

object CourtReportGenerator {
    val DEFAULT = generate()

    fun generate(
        courtReportType: CourtReportType = CourtReportTypeGenerator.DEFAULT,
        courtAppearance: CourtAppearance? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = CourtReport(id, courtAppearance, courtReportType)
}
