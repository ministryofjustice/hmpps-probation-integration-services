package uk.gov.justice.digital.hmpps.messaging.detail

import java.time.ZonedDateTime
import java.util.*

data class CheckInDetail(val checkinUuid: UUID, val notes: String, val sensitive: Boolean, val dateTime: ZonedDateTime?)