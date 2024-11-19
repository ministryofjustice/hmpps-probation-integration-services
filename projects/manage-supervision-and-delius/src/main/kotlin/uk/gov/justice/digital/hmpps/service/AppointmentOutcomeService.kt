package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.Outcome
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.AppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByTypeIdAndOutcomeCode
import java.time.LocalDateTime

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
            val dateTime = LocalDateTime.now()
            attended = outcome.attended
            complied = if (contactTypeOutcome.outcome.outcomeCompliantAcceptable!!) "Y" else "N"
            notes = outcome.notes?.let {
                """
                Comment added by ${outcome.recordedBy} on ${
                    dateTime.format(DeliusDateTimeFormatter).substring(0, 10)
                } at ${dateTime.format(DeliusDateTimeFormatter).substring(11, 16)}
                $it
            """.trimIndent()
            }
            outcomeId = contactTypeOutcome.outcome.id
            sensitive = outcome.sensitive
        }

        appointmentRepository.save(appointment)
    }
}