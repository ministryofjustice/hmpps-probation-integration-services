package uk.gov.justice.digital.hmpps.controller.entity

import java.time.LocalDate
import java.time.ZonedDateTime

interface Assessment {
    val score: Long
    val lastModifiedDateTime: ZonedDateTime
    val assessmentDate: LocalDate
}
