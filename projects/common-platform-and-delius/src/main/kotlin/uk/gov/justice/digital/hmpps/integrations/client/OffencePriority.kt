package uk.gov.justice.digital.hmpps.integrations.client

import com.fasterxml.jackson.annotation.JsonProperty

data class OffencePriority(
    @JsonProperty("ho_offence_code") val hoOffenceCode: String,
    @JsonProperty("offence_desc") val offenceDescription: String,
    @JsonProperty("priority") val priority: Int,
    @JsonProperty("offence_type") val offenceType: String,
    @JsonProperty("max_custodial_sentence") val maxCustodialSentence: String? = null,
)
