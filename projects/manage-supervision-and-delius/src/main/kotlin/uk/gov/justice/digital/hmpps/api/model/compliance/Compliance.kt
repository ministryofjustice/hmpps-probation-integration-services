package uk.gov.justice.digital.hmpps.api.model.compliance

import uk.gov.justice.digital.hmpps.api.model.overview.ActivityCount
import uk.gov.justice.digital.hmpps.api.model.overview.Compliance
import uk.gov.justice.digital.hmpps.api.model.overview.OverviewOffence
import uk.gov.justice.digital.hmpps.api.model.overview.Order

data class SentenceCompliance(
    val eventNumber: String,
    val mainOffence: OverviewOffence,
    val order: Order?,
    val activeBreach: Breach?,
    val activeRecall: Breach?,
    val rarDescription: String? = null,
    val rarCategory: String?,
    val compliance: Compliance,
    val activity: ActivityCount
)
