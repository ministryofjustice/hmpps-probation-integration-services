package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.Outcome
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcomeRepository

@Service
class AppointmentOutcomeService(
    private val appointmentRepository: AppointmentRepository,
    private val contactTypeOutcomeRepository: ContactTypeOutcomeRepository
) {

    fun recordOutcome(outcome: Outcome) {
        val appointment = appointmentRepository.findById(outcome.id).orElseThrow { throw NotFoundException("Appointment", "id", outcome.id) }

        if (!contactTypeOutcomeRepository.existsById(appointment.type.id, outcome.code)) {
            throw NotFoundException("ContactTypeOutcome", "ContactTypeOutcomeId with contact_type_id $appointment.type.id and ContactOutcome.code ", outcome.code)
        }

        appointmentRepository.save(appointment)
    }
}