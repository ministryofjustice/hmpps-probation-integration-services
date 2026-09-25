package uk.gov.justice.digital.hmpps.api.model.overview

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "OverviewProvision")
data class Provision(
    val description: String
)