package uk.gov.justice.digital.hmpps.datetime

import java.time.ZoneId
import java.time.format.DateTimeFormatter

val DeliusDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

val EuropeLondon: ZoneId = ZoneId.of("Europe/London")
