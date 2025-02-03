package uk.gov.justice.digital.hmpps.integrations.delius.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class OffenceCsvRecord(
    @JsonProperty("cjs_offence_code") val cjsOffenceCode: String,
    @JsonProperty("ho_offence_code") val hoOffenceCode: String?,
    @JsonProperty("ho_offence_desc") val hoOffenceDesc: String?,
    @JsonProperty("offence_published_desc") val offencePublishedDesc: String?,
    @JsonProperty("offence_type") val offenceType: String?,
    @JsonProperty("offence_group") val offenceGroup: String?,
    @JsonProperty("offence_detailed_desc") val offenceDetailedDesc: String?,
    @JsonProperty("offence_group_code") val offenceGroupCode: String?,
    @JsonProperty("priority") val priority: String?,
    @JsonProperty("release") val release: String?
)
