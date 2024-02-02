package uk.gov.justice.digital.hmpps.api.controller

import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.Appointment
import uk.gov.justice.digital.hmpps.service.AppointmentService
import java.time.LocalDate

@RestController
@RequestMapping("/appointments")
class AppointmentController(private val appointmentService: AppointmentService) {
    @PreAuthorize("hasAnyRole('RESETTLEMENT_PASSPORT_SUPERVISION_ACCESS','PROBATION_API__RESETTLEMENT_PASSPORT__CASE_DETAIL')")
    @GetMapping("/{crn}")
    fun findPerson(
        @PathVariable crn: String,
        @RequestParam startDate: LocalDate,
        @RequestParam endDate: LocalDate,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): ResultSet<Appointment> =
        appointmentService.findAppointmentsFor(crn, startDate, endDate, PageRequest.of(page, size)).let {
            ResultSet(it.content, it.totalElements, it.totalPages, page, size)
        }
}

data class ResultSet<T>(
    val results: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)
