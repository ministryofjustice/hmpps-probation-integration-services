package uk.gov.justice.digital.hmpps.api.model.user

import java.time.LocalDate

data class UserAlert(
    val id: Long,
    val type: UserAlertType,
    val crn: String,
    val date: LocalDate,
    val description: String?,
    val notes: String?,
    val officer: Staff
)

data class UserAlertType(val description: String, val editable: Boolean)

data class UserAlerts(
    val content: List<UserAlert>,
    val totalResults: Int,
    val totalPages: Int,
    val page: Int,
    val size: Int,
)