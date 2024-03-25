package uk.gov.justice.digital.hmpps.datetime

import java.time.ZoneId
import java.time.format.DateTimeFormatter

val DeliusDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
val DeliusDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

val EuropeLondon: ZoneId = ZoneId.of("Europe/London")
