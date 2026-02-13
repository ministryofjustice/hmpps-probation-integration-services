package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

data class SessionsResponse(
    val sessions: List<Session>
)

data class Session(
    val project: CodeDescription,
    val date: LocalDate,
    val allocatedCount: Long,
    val outcomeCount: Long,
    val enforcementActionCount: Long
)