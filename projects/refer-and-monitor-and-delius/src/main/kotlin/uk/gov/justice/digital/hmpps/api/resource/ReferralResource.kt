package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.api.model.MergeAppointment
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.service.AppointmentService
import uk.gov.justice.digital.hmpps.service.NsiService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("probation-case/{crn}/referrals")
class ReferralResource(
    private val telemetryService: TelemetryService,
    private val nsiService: NsiService,
    private val appointmentService: AppointmentService
) {
    @PreAuthorize("hasRole('PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL__RW')")
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun referralStarted(@PathVariable crn: String, @RequestBody referralStarted: ReferralStarted) {
        nsiService.startNsi(crn, referralStarted)
        telemetryService.trackEvent(
            "ReferralStarted",
            mapOf("crn" to crn, "referralId" to referralStarted.referralId.toString())
        )
    }

    @PreAuthorize("hasRole('PROBATION_API__REFER_AND_MONITOR__CASE_DETAIL__RW')")
    @PutMapping("/{referralId}/appointments")
    @ResponseStatus(HttpStatus.OK)
    fun mergeAppointment(
        @PathVariable crn: String,
        @PathVariable referralId: UUID,
        @RequestBody mergeAppointment: MergeAppointment
    ): Map<String, Long> = try {
        val deliusId = appointmentService.mergeAppointment(crn, mergeAppointment)
        telemetryService.trackEvent(
            "MergeAppointment",
            mapOf(
                "crn" to crn,
                "referralId" to mergeAppointment.referralId.toString(),
                "appointmentId" to mergeAppointment.id.toString(),
                "deliusId" to deliusId.toString(),
                "rescheduleRequestedBy" to mergeAppointment.rescheduleRequestedBy.toString(),
            )
        )
        mapOf("appointmentId" to deliusId)
    } catch (rse: ResponseStatusException) {
        telemetryService.trackEvent(
            "PastAppointmentWithoutOutcome",
            mapOf(
                "crn" to crn,
                "referralId" to mergeAppointment.referralId.toString(),
                "referralReference" to mergeAppointment.referralReference,
                "appointmentId" to mergeAppointment.id.toString(),
                "startTime" to mergeAppointment.start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "outcome" to (mergeAppointment.outcome?.attended?.name ?: "null")
            )
        )
        throw rse
    }
}
