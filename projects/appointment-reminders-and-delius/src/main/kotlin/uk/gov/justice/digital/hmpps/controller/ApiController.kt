package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.config.CsvMapperConfig.csvMapper
import uk.gov.justice.digital.hmpps.model.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.repository.UpwAppointmentRepository
import java.time.LocalDate

@RestController
class ApiController(
    private val upwAppointmentRepository: UpwAppointmentRepository
) {
    @GetMapping("/upw-appointments.csv", produces = ["text/csv"])
    @PreAuthorize("hasRole('PROBATION_API__REMINDERS__UPW_APPOINTMENTS')")
    fun handle(
        @RequestParam providerCode: String,
        @RequestParam date: LocalDate = LocalDate.now().plusDays(2)
    ): String = csvMapper
        .writer(csvMapper.schemaFor(UnpaidWorkAppointment::class.java).withHeader())
        .writeValueAsString(upwAppointmentRepository.getUnpaidWorkAppointments(date, providerCode))
}
