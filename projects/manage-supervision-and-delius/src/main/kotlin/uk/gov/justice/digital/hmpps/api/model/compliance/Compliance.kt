package uk.gov.justice.digital.hmpps.api.model.compliance

import uk.gov.justice.digital.hmpps.api.model.overview.*

data class SentenceCompliance(
    val eventNumber: String,
    val mainOffence: Offence,
    val order: Order?,
    val activeBreach: Breach?,
    val rar: Rar?,
    val rarCategory: String?,
    val compliance: Compliance,
    val activity: ActivityCount
)
