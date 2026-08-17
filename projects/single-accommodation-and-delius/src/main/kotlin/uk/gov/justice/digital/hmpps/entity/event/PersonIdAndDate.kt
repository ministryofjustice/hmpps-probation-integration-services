package uk.gov.justice.digital.hmpps.entity.event

import java.time.LocalDate

interface PersonIdAndDate {
    val personId: Long
    val releaseDate: LocalDate?
}