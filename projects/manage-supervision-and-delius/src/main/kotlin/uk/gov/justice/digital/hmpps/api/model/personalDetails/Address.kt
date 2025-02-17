package uk.gov.justice.digital.hmpps.api.model.personalDetails

import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import java.time.LocalDate

data class Address(
    val id: Long?,
    val buildingName: String?,
    val buildingNumber: String?,
    val streetName: String?,
    val district: String?,
    val town: String?,
    val county: String?,
    val postcode: String?,
    val telephoneNumber: String?,
    val from: LocalDate,
    val to: LocalDate?,
    val verified: Boolean?,
    val lastUpdated: LocalDate?,
    val lastUpdatedBy: Name?,
    val type: String?,
    val typeCode: String?,
    val status: String?,
    val addressNotes: List<NoteDetail>? = null,
    val addressNote: NoteDetail? = null,
    val noFixedAddress: Boolean?
) {
    companion object {
        fun from(
            id: Long? = null,
            buildingName: String? = null,
            buildingNumber: String? = null,
            streetName: String? = null,
            district: String? = null,
            town: String? = null,
            county: String? = null,
            postcode: String? = null,
            telephoneNumber: String?,
            from: LocalDate,
            to: LocalDate? = null,
            verified: Boolean? = null,
            lastUpdated: LocalDate? = null,
            lastUpdatedBy: Name? = null,
            type: String? = null,
            typeCode: String?,
            status: String? = null,
            addressNotes: List<NoteDetail>? = null,
            addressNote: NoteDetail? = null,
            noFixedAddress: Boolean?
        ): Address? =
            if (
                buildingName == null && buildingNumber == null && streetName == null &&
                district == null && town == null && county == null && postcode == null && typeCode == null
                && verified == null
            ) {
                null
            } else {
                Address(
                    id,
                    buildingName,
                    buildingNumber,
                    streetName,
                    district,
                    town,
                    county,
                    postcode,
                    telephoneNumber,
                    from,
                    to,
                    verified,
                    lastUpdated,
                    lastUpdatedBy,
                    type,
                    typeCode,
                    status,
                    addressNotes,
                    addressNote,
                    noFixedAddress
                )
            }
    }
}