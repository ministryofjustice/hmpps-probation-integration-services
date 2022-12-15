package uk.gov.justice.digital.hmpps.integrations.prison

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDate

data class Booking(
    @JsonAlias("bookingId")
    val id: Long,
    val bookingNo: String,
    @JsonAlias("activeFlag")
    val active: Boolean,
    val offenderNo: String,
    val agencyId: String? = null,
    val locationDescription: String? = null,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val recall: Boolean? = null,
    val legalStatus: String? = null
)
