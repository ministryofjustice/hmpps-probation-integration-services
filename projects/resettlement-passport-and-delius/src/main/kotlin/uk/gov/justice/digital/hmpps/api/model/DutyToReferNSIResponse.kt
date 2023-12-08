package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate
import java.time.ZonedDateTime

data class DutyToReferNSI(
    val nsiSubType: String,
    val referralDate: LocalDate,
    val provider: String?,
    val team: String?,
    val officer: Officer?,
    val status: String,
    val startDateTime: ZonedDateTime?,
    val notes: String? = null,
    val mainAddress: MainAddress? = null,
)

data class MainAddress(
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val noFixedAbode: Boolean?,
)

data class Officer(
    val forename: String,
    val surname: String,
    val middleName: String? = null,
)
