package uk.gov.justice.digital.hmpps.integrations.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

interface OsClient {
    @GetExchange("/search/places/v1/find")
    fun searchByFreeText(
        @RequestParam query: String,
        @RequestParam maxResults: Int,
        @RequestParam key: String
    ): OsPlacesResponse
}

data class OsPlacesResponse(
    val header: OsPlacesHeader,
    val results: List<DpaWrapper>?
)

data class OsPlacesHeader(
    val uri: String?,
    val query: String?,
    val offset: Int?,
    val totalresults: Int?,
    val format: String?,
    val dataset: String?,
    val lr: String?,
    val maxresults: Int?,
    val matchprecision: Double?,
    val filter: String?,
    val srs: String?,
    val epoch: String?,
    val lastupdate: String?,
    val output_srs: String?
)

class DpaWrapper {
    @JsonProperty("DPA")
    val dpa: Dpa? = null
}

data class Dpa(
    @JsonProperty("UPRN")
    val uprn: String?,
    @JsonProperty("UDPRN")
    val udprn: String?,
    @JsonProperty("ADDRESS")
    val address: String?,
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
    val postTown: String?,
    @JsonProperty("POSTCODE")
    val postcode: String?,
    @JsonProperty("RPC")
    val rpc: String?,
    @JsonProperty("X_COORDINATE")
    val xCoordinate: Double?,
    @JsonProperty("Y_COORDINATE")
    val yCoordinate: Double?,
    @JsonProperty("LNG")
    val lng: Double?,
    @JsonProperty("LAT")
    val lat: Double?,
    @JsonProperty("STATUS")
    val status: String?,
    @JsonProperty("MATCH")
    val match: Float?,
    @JsonProperty("MATCH_DESCRIPTION")
    val matchDescription: String?,
    @JsonProperty("LANGUAGE")
    val language: String?,
    @JsonProperty("COUNTRY_CODE")
    val countryCode: String?,
    @JsonProperty("COUNTRY_CODE_DESCRIPTION")
    val countryCodeDescription: String?,
    @JsonProperty("LOCAL_CUSTODIAN_CODE")
    val localCustodianCode: Int?,
    @JsonProperty("LOCAL_CUSTODIAN_CODE_DESCRIPTION")
    val localCustodianCodeDescription: String?,
    @JsonProperty("CLASSIFICATION_CODE")
    val classificationCode: String?,
    @JsonProperty("CLASSIFICATION_CODE_DESCRIPTION")
    val classificationCodeDescription: String?,
    @JsonProperty("POSTAL_ADDRESS_CODE")
    val postalAddressCode: String?,
    @JsonProperty("POSTAL_ADDRESS_CODE_DESCRIPTION")
    val postalAddressCodeDescription: String?,
    @JsonProperty("BLPU_STATE_CODE")
    val blpuStateCode: String?,
    @JsonProperty("BLPU_STATE_CODE_DESCRIPTION")
    val blpuStateCodeDescription: String?,
    @JsonProperty("TOPOGRAPHY_LAYER_TOID")
    val topographyLayerToid: String?,
    @JsonProperty("PARENT_UPRN")
    val parentUprn: String?,
    @JsonProperty("LAST_UPDATE_DATE")
    val lastUpdateDate: String?,
    @JsonProperty("ENTRY_DATE")
    val entryDate: String?,
    @JsonProperty("LEGAL_NAME")
    val legalName: String?,
    @JsonProperty("BLPU_STATE_DATE")
    val blpuStateDate: String?,
    @JsonProperty("DELIVERY_POINT_SUFFIX")
    val deliveryPointSuffix: String?,
    @JsonProperty("PARISH_CODE")
    val parishCode: String?,
    @JsonProperty("WARD_CODE")
    val wardCode: String?
)