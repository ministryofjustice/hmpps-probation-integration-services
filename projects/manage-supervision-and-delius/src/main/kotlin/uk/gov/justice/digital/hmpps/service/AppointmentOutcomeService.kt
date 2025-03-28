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
        val appointment = appointmentRepository.findById(outcome.id)
            .orElseThrow { throw NotFoundException("Appointment", "id", outcome.id) }

        val contactTypeOutcome =
            contactTypeOutcomeRepository.getByTypeIdAndOutcomeCode(appointment.type.id, outcome.code)

        appointment.apply {
            attended = outcome.attended
            complied = if (contactTypeOutcome.outcome.outcomeCompliantAcceptable!!) "Y" else "N"
            notes = listOfNotNull(notes, outcome.notes).joinToString(System.lineSeparator())
            outcomeId = contactTypeOutcome.outcome.id
            sensitive = outcome.sensitive
        }

        appointmentRepository.save(appointment)
    }
}