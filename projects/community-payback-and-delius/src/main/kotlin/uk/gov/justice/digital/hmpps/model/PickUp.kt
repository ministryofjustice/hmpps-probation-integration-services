package uk.gov.justice.digital.hmpps.model

import java.time.LocalTime

data class PickUp(val time: LocalTime?, val locationDescription: String?, val location: Code?)