package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class Registrations(
    val registrations: List<Registration>? = null
)

data class Registration(
    val registrationId: Long? = null,
    val offenderId: Long? = null,
    val register: KeyValue? = null,
    val type: KeyValue? = null,
    val riskColour: String? = null,
    val startDate: LocalDate? = null,
    val nextReviewDate: LocalDate? = null,
    val reviewPeriodMonths: Long? = null,
    val notes: String? = null,
    val registeringTeam: KeyValue? = null,
    val registeringOfficer: StaffHuman? = null,
    val registeringProbationArea: KeyValue? = null,
    val registerLevel: KeyValue? = null,
    val registerCategory: KeyValue? = null,
    val warnUser: Boolean = false,
    val active: Boolean = false,
    val endDate: LocalDate? = null,
    val deregisteringTeam: KeyValue? = null,
    val deregisteringOfficer: StaffHuman? = null,
    val deregisteringProbationArea: KeyValue? = null,
    val deregisteringNotes: String? = null,
    val numberOfPreviousDeregistrations: Int = 0
)
