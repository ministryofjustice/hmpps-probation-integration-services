package uk.gov.justice.digital.hmpps.api.model.overview

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
)