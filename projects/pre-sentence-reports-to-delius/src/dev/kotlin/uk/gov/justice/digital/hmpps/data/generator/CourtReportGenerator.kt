package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator.DEFAULT_COURT_APPEARANCE
import uk.gov.justice.digital.hmpps.entity.CourtReport

object CourtReportGenerator {
    val DEFAULT_COURT_REPORT = CourtReport(IdGenerator.getAndIncrement(), DEFAULT_COURT_APPEARANCE)
}