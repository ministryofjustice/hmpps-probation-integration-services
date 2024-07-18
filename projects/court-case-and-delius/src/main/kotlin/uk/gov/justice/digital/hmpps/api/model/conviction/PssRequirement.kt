package uk.gov.justice.digital.hmpps.api.model.conviction

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.api.model.KeyValue

data class PssRequirement(
    val type: KeyValue?,
    val subType: KeyValue?,
    @Schema(description = "Is the requirement currently active")
    val active: Boolean
)