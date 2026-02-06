package uk.gov.justice.digital.hmpps.model

import java.time.LocalTime

data class PickUp(val time: LocalTime?, val location: PickUpLocation?)

@Deprecated("Use [PickUp]")
data class LegacyPickUp(val time: LocalTime?, val location: Code?)
