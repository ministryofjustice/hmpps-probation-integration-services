package uk.gov.justice.digital.hmpps.controller

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.service.CommunityPaybackAppointmentsService
import uk.gov.justice.digital.hmpps.utils.Extensions.mapSorts
import java.time.LocalDate

@RestController
@RequestMapping("/appointments")
@PreAuthorize("hasRole('PROBATION_API__COMMUNITY_PAYBACK__CASE_DETAIL')")
class AppointmentsController(
    private val communityPaybackAppointmentsService: CommunityPaybackAppointmentsService
) {
    @GetMapping("")
    fun getAppointments(
        @RequestParam(required = false) crn: String?,
        @RequestParam(required = false) fromDate: LocalDate?,
        @RequestParam(required = false) toDate: LocalDate?,
        @RequestParam(required = false) projectCodes: List<String>?,
        @RequestParam(required = false) projectTypeCodes: List<String>?,
        @RequestParam(required = false) outcomeCode: List<String>?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "100") size: Int
    ) = communityPaybackAppointmentsService.getAppointments(
        crn, fromDate, toDate,
        projectCodes, projectTypeCodes, outcomeCode,
        PageRequest.of(page, size).mapSorts(
            "name" to "name",
            "date" to "date"
        )
    )
}