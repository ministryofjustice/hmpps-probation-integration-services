package uk.gov.justice.digital.hmpps.datetime

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal

val DeliusDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
val DeliusDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
fun Temporal.toDeliusDate(): String = DeliusDateFormatter.format(this)
fun Temporal.toDeliusDateTime(): String = DeliusDateTimeFormatter.format(this)

val EuropeLondon: ZoneId = ZoneId.of("Europe/London")
