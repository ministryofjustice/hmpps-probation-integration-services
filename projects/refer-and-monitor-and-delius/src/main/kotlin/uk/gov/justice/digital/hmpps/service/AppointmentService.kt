package uk.gov.justice.digital.hmpps.service

import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.AppointmentOutcome.Code
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.getAppointmentById
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.getByCode
import uk.gov.justice.digital.hmpps.messaging.Attended
import uk.gov.justice.digital.hmpps.messaging.UpdateAppointmentOutcome
import java.time.ZonedDateTime

// @Service deactivated until completed
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val typeRepository: AppointmentTypeRepository,
    private val outcomeRepository: AppointmentOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val enforcementRepository: EnforcementRepository
) {
    fun updateOutcome(uao: UpdateAppointmentOutcome) {
        val appointment = appointmentRepository.getAppointmentById(uao.id)
        val outcome = outcomeRepository.getByCode(attendanceOutcome(uao).value)
        appointment.outcome = outcome
        appointment.notes += """${System.lineSeparator()}
            |----------
            |
            |${uao.notes}
        """.trimMargin()
        if (uao.notify) {
            val action = enforcementActionRepository.getByCode(EnforcementAction.Code.REFER_TO_PERSON_MANAGER.value)
            val enforcement = Enforcement(appointment, action, action.responseByPeriod?.let { ZonedDateTime.now().plusDays(it) })
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
