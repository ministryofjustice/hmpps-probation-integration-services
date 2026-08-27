package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class CreateProjectsRequest(
    val projects: List<CreateProjectRequest>
)

data class CreateProjectRequest(
    val providerCode: String,
    val pduCode: String? = null,
    val localAdminUnitCode: String? = null,
    val teamCode: String,
    val projectTypeCode: String,
    val code: String,
    val name: String,
    val defaultPickupPointCode: String,
    val hiVisRequired: Boolean = false,
    val reportToSite: Boolean = false,
    val startDate: LocalDate,
    val expectedEndDate: LocalDate? = null,
    val completionDate: LocalDate? = null,
    val beneficiaryDetails: CreateProjectBeneficiaryDetails = CreateProjectBeneficiaryDetails(),
    val placementDetails: CreateProjectPlacementDetails = CreateProjectPlacementDetails(),
)

data class CreateProjectBeneficiaryDetails(
    val beneficiary: String? = null,
    val contactName: String? = null,
    val emailAddress: String? = null,
    val website: String? = null,
    val telephoneNumber: String? = null,
    val buildingName: String? = null,
    val buildingNumber: String? = null,
    val streetName: String? = null,
    val townCity: String? = null,
    val county: String? = null,
    val postcode: String? = null,
    val serviceLevelAgreement: Boolean = false,
    val slaStartDate: LocalDate? = null,
    val slaEndDate: LocalDate? = null,
    val contribution: Boolean = false,
    val additionalDetails: String? = null,
)

data class CreateProjectPlacementDetails(
    val locationDescription: String? = null,
    val buildingName: String? = null,
    val buildingNumber: String? = null,
    val streetName: String? = null,
    val townCity: String? = null,
    val county: String? = null,
    val postcode: String? = null,
    val contactName: String? = null,
    val telephoneNumber: String? = null,
    val emailAddress: String? = null,
    val url: String? = null,
    val placementNotes: String? = null,
)
