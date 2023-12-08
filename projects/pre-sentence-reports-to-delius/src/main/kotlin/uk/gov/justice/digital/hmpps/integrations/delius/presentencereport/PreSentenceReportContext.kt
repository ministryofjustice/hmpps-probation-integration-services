package uk.gov.justice.digital.hmpps.integrations.delius.presentencereport

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PreSentenceReportContext(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate,
    val pnc: String?,
    val address: Address?,
    val mainOffence: Offence,
    val otherOffences: List<Offence>?,
    val court: Court,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Name(
    val forename: String,
    val surname: String,
    val middleName: String?,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Address(
    val noFixedAbode: Boolean,
    val buildingName: String?,
    val addressNumber: String?,
    val streetName: String?,
    val town: String?,
    val district: String?,
    val county: String?,
    val postcode: String?,
)

data class Offence(
    val description: String,
)

data class Court(
    val name: String,
    val localJusticeArea: LocalJusticeArea,
)

data class LocalJusticeArea(
    val name: String,
)
