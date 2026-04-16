package uk.gov.justice.digital.hmpps.model

import org.springframework.data.domain.Page
import org.springframework.data.web.PagedModel
import java.time.LocalDate

class SessionsResponse(
    @Deprecated("Use 'content' field for paged results", replaceWith = ReplaceWith("content"))
    val sessions: List<Session>,
    page: Page<Session>,
) : PagedModel<Session>(page)

data class Session(
    val project: CodeDescription,
    val date: LocalDate,
    val allocatedCount: Long,
    val outcomeCount: Long,
    val enforcementActionCount: Long
)