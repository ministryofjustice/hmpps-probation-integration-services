package uk.gov.justice.digital.hmpps.integrations.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

interface OsClient {
    @GetExchange("/search/places/v1/find")
    fun searchByFreeText(
        @RequestParam query: String,
        @RequestParam maxResults: Int,
        @RequestParam minMatch: Double,
    ): OsPlacesResponse
}

data class OsPlacesResponse(
    val header: OsPlacesHeader,
    val results: List<DpaWrapper>?
)

data class OsPlacesHeader(
    @JsonProperty("uri")
    val uri: String?,
    @JsonProperty("query")
    val query: String?,
    @JsonProperty("offset")
    val offset: Int?,
    @JsonProperty("totalresults")
    val totalResults: Int?,
    @JsonProperty("format")
    val format: String?,
    @JsonProperty("dataset")
    val dataset: String?,
    @JsonProperty("lr")
    val lr: String?,
    @JsonProperty("maxresults")
    val maxResults: Int?,
    @JsonProperty("matchprecision")
    val matchPrecision: Double?,
    @JsonProperty("filter")
    val filter: String?,
    @JsonProperty("srs")
    val srs: String?,
    @JsonProperty("epoch")
    val epoch: String?,
    @JsonProperty("lastupdate")
    val lastUpdate: String?,
    @JsonProperty("output_srs")
    val outputSrs: String?
)

class DpaWrapper {
    @JsonProperty("DPA")
    val dpa: Dpa? = null
}

data class Dpa(
    @JsonProperty("UPRN")
    val uprn: Long,
    @JsonProperty("ADDRESS")
    val address: String,
    @JsonProperty("PO_BOX_NUMBER")
    val poBoxNumber: String?,
    @JsonProperty("ORGANISATION_NAME")
    val organisationName: String?,
    @JsonProperty("DEPARTMENT_NAME")
    val departmentName: String?,
    @JsonProperty("SUB_BUILDING_NAME")
    val subBuildingName: String?,
    @JsonProperty("BUILDING_NAME")
    val buildingName: String?,
    @JsonProperty("BUILDING_NUMBER")
    val buildingNumber: Int?,
    @JsonProperty("DEPENDENT_THOROUGHFARE_NAME")
    val dependentThoroughfareName: String?,
    @JsonProperty("THOROUGHFARE_NAME")
    val thoroughfareName: String?,
    @JsonProperty("DOUBLE_DEPENDENT_LOCALITY")
    val doubleDependentLocality: String?,
    @JsonProperty("DEPENDENT_LOCALITY")
    val dependentLocality: String?,
    @JsonProperty("POST_TOWN")
    val postTown: String,
    @JsonProperty("POSTCODE")
    val postcode: String,
    @JsonProperty("LOCAL_CUSTODIAN_CODE_DESCRIPTION")
    val localCustodianCodeDescription: String,
)