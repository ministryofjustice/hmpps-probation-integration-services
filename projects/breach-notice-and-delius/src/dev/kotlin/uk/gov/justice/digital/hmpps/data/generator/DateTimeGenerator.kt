package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object DateTimeGenerator {
    fun zonedDateTime(): ZonedDateTime = ZonedDateTime.now(EuropeLondon).truncatedTo(ChronoUnit.SECONDS)
}