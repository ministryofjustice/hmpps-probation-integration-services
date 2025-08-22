package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.ContactRepository

@Service
class AppointmentService(
    private val contactRepository: ContactRepository,
) {
    fun getAppointments(request: GetAppointmentsRequest) = with(request) {
        if (toDate < fromDate) throw IllegalArgumentException("toDate cannot be before fromDate")

        GetAppointmentsResponse(
            contactRepository.findAllByComponentIdInDateRange(requirementIds, licenceConditionIds, fromDate, toDate)
                .map {
                    AppointmentResponse(
                        crn = it.person.crn,
                        reference = it.externalReference?.takeLast(36),
                        requirementId = it.requirement?.id,
                        licenceConditionId = it.licenceCondition?.id,
                        date = it.date,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        outcome = it.outcome?.run { AppointmentOutcome(code, description, attended, complied) },
                        location = it.location?.run { CodedValue(code, description) },
                        staff = it.staff.toProbationPractitioner { null },
                        team = it.team.toCodedValue(),
                        notes = it.notes,
                        sensitive = it.sensitive
                    )
                }
        )
    }
}
