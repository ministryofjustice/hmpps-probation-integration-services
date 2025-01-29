package uk.gov.justice.digital.hmpps.api.model.personalDetails

import jakarta.validation.constraints.Size
import java.time.LocalDate

data class PersonalContactEditRequest(
    @field:Size(max = 35)
    val phoneNumber: String? = null,
    @field:Size(max = 35)
    val mobileNumber: String? = null,
    @field:Size(max = 255)
    val emailAddress: String? = null,
    @field:Size(max = 35)
    val buildingName: String? = null,
    @field:Size(max = 35)
    val buildingNumber: String? = null,
    @field:Size(max = 35)
    val streetName: String? = null,
    @field:Size(max = 35)
    val town: String? = null,
    @field:Size(max = 35)
    val county: String? = null,
    @field:Size(max = 35)
    var postcode: String? = null,
    val addressTypeCode: String? = null,
    val verified: Boolean? = null,
    val noFixedAddress: Boolean? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val notes: String? = null
)
