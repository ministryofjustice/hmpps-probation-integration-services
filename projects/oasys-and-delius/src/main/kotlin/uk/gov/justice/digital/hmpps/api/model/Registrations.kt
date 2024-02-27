package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class Registrations(
    val registrations: List<Registration>
)

data class Registration(
    val registrationId: Long,
    val offenderId: Long,
    val register: CodeDescription?,
    val type: CodeDescription,
    val riskColour: String?,
    val startDate: LocalDate,
    val nextReviewDate: LocalDate?,
    val reviewPeriodMonths: Long?,
    val notes: String?,
    val registeringTeam: CodeDescription,
    val registeringOfficer: Officer,
    val registeringProbationArea: CodeDescription,
    val registerLevel: CodeDescription?,
    val registerCategory: CodeDescription?,
    val warnUser: Boolean,
    val active: Boolean,
    val registrationReviews: List<Review> = listOf(),
)

data class Review(
    val reviewDate: LocalDate,
    val reviewDateDue: LocalDate?,
    val notes: String?,
    val reviewingTeam: CodeDescription,
    val reviewingOfficer: Officer,
    val completed: Boolean
)