package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.CourtAppearance

object CourtAppearanceGenerator {
    val DEFAULT_COURT_APPEARANCE = CourtAppearance(IdGenerator.getAndIncrement(), EventGenerator.DEFAULT_EVENT)
}