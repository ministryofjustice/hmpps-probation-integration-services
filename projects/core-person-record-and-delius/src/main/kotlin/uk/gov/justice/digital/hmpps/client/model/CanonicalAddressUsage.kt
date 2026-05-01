package uk.gov.justice.digital.hmpps.client.model

import com.fasterxml.jackson.annotation.JsonUnwrapped

data class CanonicalAddressUsage(
    @JsonUnwrapped
    val usageCode: CanonicalAddressUsageCode,
    val isActive: Boolean,
)