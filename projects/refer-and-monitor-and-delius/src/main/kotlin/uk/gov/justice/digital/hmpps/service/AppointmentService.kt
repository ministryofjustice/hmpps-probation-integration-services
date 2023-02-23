package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.AppointmentOutcome.Code
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.contact.getAppointmentById
import uk.gov.justice.digital.hmpps.integrations.delius.contact.getByCode
import uk.gov.justice.digital.hmpps.messaging.Attended
import uk.gov.justice.digital.hmpps.messaging.UpdateAppointmentOutcome
import java.time.ZonedDateTime

// @Service deactivated until completed
class AppointmentService(
    private val contactRepository: ContactRepository,
    private val typeRepository: ContactTypeRepository,
    private val outcomeRepository: ContactOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val enforcementRepository: EnforcementRepository
) {
    fun updateOutcome(uao: UpdateAppointmentOutcome) {
        val appointment = contactRepository.getAppointmentById(uao.id)
        val outcome = outcomeRepository.getByCode(attendanceOutcome(uao).value)
        appointment.outcome = outcome
        appointment.notes += """${System.lineSeparator()}
            |----------
            |
            |${uao.notes}
        """.trimMargin()
        if (uao.notify) {
            val action = enforcementActionRepository.getByCode(EnforcementAction.Code.REFER_TO_PERSON_MANAGER.value)
            val enforcement =
                Enforcement(appointment, action, action.responseByPeriod?.let { ZonedDateTime.now().plusDays(it) })
            enforcementRepository.save(enforcement)
            appointment.enforcementActionId = action.id
            appointment.enforcement = true
        }
    }

    companion object {
        private fun attendanceOutcome(uoa: UpdateAppointmentOutcome): Code =
            when (uoa.attended) {
                Attended.YES, Attended.LATE -> if (uoa.notify) Code.FAILED_TO_COMPLY else Code.COMPLIED
                Attended.NO -> Code.FAILED_TO_ATTEND
            }
    }
}
