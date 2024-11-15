package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.Outcome
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByTypeIdAndOutcomeCode

@Service
class AppointmentOutcomeService(
    private val appointmentRepository: AppointmentRepository,
    private val contactTypeOutcomeRepository: ContactTypeOutcomeRepository
) {

    fun recordOutcome(outcome: Outcome) {
        val appointment = appointmentRepository.findById(outcome.id).orElseThrow { throw NotFoundException("Appointment", "id", outcome.id) }

        val contactTypeOutcome = contactTypeOutcomeRepository.getByTypeIdAndOutcomeCode(appointment.type.id, outcome.code)

        appointment.apply {
            attended = if (contactTypeOutcome.type.attendanceContact) "Y" else "N"
            notes = outcome.notes
            sensitive = outcome.sensitive
        }

        appointmentRepository.save(appointment)
    }
}