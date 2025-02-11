package uk.gov.justice.digital.hmpps.api.model.overview

import uk.gov.justice.digital.hmpps.api.model.risk.MappaDetail

data class Overview(
    val appointmentsWithoutOutcome: Int = 0,
    val absencesWithoutEvidence: Int = 0,
    val activity: ActivityCount?,
    val compliance: Compliance?,
    val personalDetails: PersonalDetails,
    val previousOrders: PreviousOrders,
    val schedule: Schedule,
    val sentences: List<Sentence>,
    val registrations: List<String>,
    val mappa: MappaDetail? = null
)