package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.SessionsService
import uk.gov.justice.digital.hmpps.utils.Extensions.mapSorts
import java.time.LocalDate

@RestController
@RequestMapping("/sessions")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class SessionsController(
    private val sessionsService: SessionsService
) {

    @GetMapping
    fun getSessionsForTeams(
        @RequestParam teamCodes: List<String>,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @RequestParam typeCode: List<String> = emptyList(),
        @PageableDefault(page = 0, size = 10, sort = ["date", "projectName"]) pageable: Pageable
    ) = sessionsService.getSessions(
        teamCodes, startDate, endDate, typeCode, pageable.mapSorts(
            "projectName" to """lower("projectName")""",
            "date" to """"appointmentDate"""",
            "allocatedCount" to """coalesce("allocatedCount", 0)""",
            "outcomeCount" to """coalesce("outcomeCount", 0)"""
        )
    )
}