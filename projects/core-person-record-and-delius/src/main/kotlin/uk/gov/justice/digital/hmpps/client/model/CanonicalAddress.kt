package uk.gov.justice.digital.hmpps.client.model

import java.time.ZonedDateTime

data class CanonicalAddress(
    val cprAddressId: String,
    val deliusAddressId: Long? = null,
    val noFixedAbode: Boolean? = null,
    val startDate: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val postcode: String? = null,
    val subBuildingName: String? = null,
    val buildingName: String? = null,
    val buildingNumber: String? = null,
    val thoroughfareName: String? = null,
    val dependentLocality: String? = null,
    val postTown: String? = null,
    val county: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val uprn: String? = null,
    val status: CanonicalAddressStatus,
    val comment: String? = null,
    val usages: List<CanonicalAddressUsage> = emptyList(),
    val typeVerified: Boolean? = null,
)