package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.CourtAppearance

object CourtAppearanceGenerator {
    val DEFAULT_COURT_APPEARANCE = CourtAppearance(IdGenerator.getAndIncrement(), EventGenerator.DEFAULT_EVENT)
}