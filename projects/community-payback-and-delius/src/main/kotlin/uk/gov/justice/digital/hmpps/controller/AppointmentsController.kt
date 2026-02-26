package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.CommunityPaybackAppointmentsService
import uk.gov.justice.digital.hmpps.utils.Extensions.mapSorts
import java.time.LocalDate

@RestController
@RequestMapping("/appointments")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class AppointmentsController(
    private val communityPaybackAppointmentsService: CommunityPaybackAppointmentsService
) {
    @GetMapping
    fun getAppointments(
        @RequestParam(required = true) username: String,
        @RequestParam(required = false) crn: String?,
        @RequestParam(required = false) fromDate: LocalDate?,
        @RequestParam(required = false) toDate: LocalDate?,
        @RequestParam(required = false) projectCodes: List<String>?,
        @RequestParam(required = false) projectTypeCodes: List<String>?,
        @RequestParam(required = false) outcomeCodes: List<String>?,
        @PageableDefault(page = 0, size = 10, sort = ["name"]) pageable: Pageable
    ) = communityPaybackAppointmentsService.getAppointments(
        username, crn, fromDate, toDate,
        projectCodes, projectTypeCodes, outcomeCodes,
        pageable.mapSorts("name" to "lower(person.forename || person.surname)", "date" to "date")
    )
}