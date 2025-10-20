package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.appointment.Outcome
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.SentenceAppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByTypeIdAndOutcomeCode

@Transactional
@Service
class AppointmentOutcomeService(
    private val sentenceAppointmentRepository: SentenceAppointmentRepository,
    private val contactTypeOutcomeRepository: ContactTypeOutcomeRepository
) {

    fun recordOutcome(outcome: Outcome) {
        val appointment = sentenceAppointmentRepository.findById(outcome.id)
            .orElseThrow { throw NotFoundException("Appointment", "id", outcome.id) }


        appointment.apply {
            if (outcome.outcomeRecorded) {
                val contactTypeOutcome =
                    contactTypeOutcomeRepository.getByTypeIdAndOutcomeCode(appointment.type.id, "ATTC")
                attended = "Y"
                complied = "Y"
                outcomeId = contactTypeOutcome.outcome.id
            }
            notes = listOfNotNull(notes, outcome.notes).joinToString(System.lineSeparator())
            if (outcome.sensitive && (sensitive != true)) {
                sensitive = true
            }
        }
    }
}