package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtAppearance
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtReport
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CourtReportType

object CourtReportGenerator {

    val DEFAULT_TYPE = CourtReportType(IdGenerator.getAndIncrement(), "Pre-Sentence Report - Fast")
    val COURT_REPORT = generate(DEFAULT_TYPE, CourtAppearanceGenerator.COURT_APPEARANCE)
    fun generate(
        courtReportType: CourtReportType?,
        courtAppearance: CourtAppearance
    ) = CourtReport(IdGenerator.getAndIncrement(), courtReportType, courtAppearance)
}