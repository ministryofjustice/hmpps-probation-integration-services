package uk.gov.justice.digital.hmpps.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Duration
import java.time.ZonedDateTime

data class Appointment(
    val type: Type,
    val dateTime: ZonedDateTime,
    @Schema(
        type = "string",
        format = "duration",
        example = "PT30M",
        description = "ISO-8601 representation of the duration"
    )
    val duration: Duration,
    val staff: Staff,
    val location: Location?,
    val description: String,
    val outcome: Outcome?
) {
    data class Type(val code: String, val description: String)
    data class Outcome(val code: String, val description: String)
}

data class Name(val forename: String, val surname: String)
data class Staff(val code: String, val name: Name, val email: String?, val telephone: String?)

data class Location(
    val code: String,
    val description: String,
    val address: Address?,
    val telephoneNumber: String?
)

data class Address(
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?
) {
    companion object {
        fun from(
            buildingName: String? = null,
            buildingNumber: String? = null,
            streetName: String? = null,
            district: String? = null,
            town: String? = null,
            county: String? = null,
            postcode: String? = null
        ): Address? =
            if (
                buildingName == null && buildingNumber == null && streetName == null &&
                district == null && town == null && county == null && postcode == null
            ) {
                null
            } else {
                Address(buildingName, buildingNumber, streetName, district, town, county, postcode)
            }
    }
}
