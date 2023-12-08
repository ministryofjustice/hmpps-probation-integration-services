package uk.gov.justice.digital.hmpps.integrations.prison

import com.fasterxml.jackson.annotation.JsonAlias

data class Booking(
    @JsonAlias("bookingId")
    val id: Long,
    val bookingNo: String,
    @JsonAlias("activeFlag")
    val active: Boolean,
    val offenderNo: String,
)
