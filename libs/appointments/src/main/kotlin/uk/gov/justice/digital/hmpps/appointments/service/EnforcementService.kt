package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Enforcement
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.EnforcementAction
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.AppointmentRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.EnforcementRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.EventRepository
import uk.gov.justice.digital.hmpps.logging.Logger.logger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional
internal class EnforcementService(
    private val enforcementRepository: EnforcementRepository,
    private val appointmentRepository: AppointmentRepository,
    private val eventRepository: EventRepository,
) {
    fun applyEnforcementAction(appointment: AppointmentContact, action: EnforcementAction?, reviewType: Type) {
        appointment.applyEnforcementAction(action)
        appointment.updateFailureToComplyCount()
        appointment.checkFailureToComplyLimit(reviewType)
    }

    fun removeEnforcementAction(appointment: AppointmentContact) {
        appointment.applyEnforcementAction()
        appointment.updateFailureToComplyCount()
    }

    fun AppointmentContact.applyEnforcementAction(action: EnforcementAction? = null) {
        enforcementActionId = action?.id
        enforcementFlag = if (action?.outstandingContactAction == true) true else null

        val existingEnforcement = id?.let { enforcementRepository.findByContactId(id) }
        if (action == null) {
            existingEnforcement?.apply { softDeleted = true }
        } else if (existingEnforcement?.action?.id != action.id) {
            enforcementRepository.save(existingEnforcement?.apply {
                this.action = action
                this.responseDate = action.responseByPeriod?.let { ZonedDateTime.now().plusDays(it) }
            } ?: Enforcement(
                contact = this,
                action = action,
                responseDate = action.responseByPeriod?.let { ZonedDateTime.now().plusDays(it) }
            ))
            createEnforcementActionContact(action)
            notes = listOfNotNull(
                notes,
                """
                    ${DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now())}
                    Enforcement Action: ${action.description}
                    """.trimIndent()
            ).joinToString("\n\n")
        }
    }

    private fun AppointmentContact.updateFailureToComplyCount() {
        val event = event() ?: return
        appointmentRepository.saveAndFlush(this)
        event.ftcCount = appointmentRepository.countFailureToComply(event)
    }

    private fun AppointmentContact.checkFailureToComplyLimit(reviewType: Type) {
        val event = event() ?: return
        val ftcLimit = event.disposal?.type?.ftcLimit ?: return
        if ((event.ftcCount ?: 0) >= ftcLimit &&
            !appointmentRepository.enforcementReviewExists(event.id, event.breachEnd)
        ) {
            createEnforcementReviewContact(reviewType)
        }
    }

    private fun AppointmentContact.createEnforcementActionContact(action: EnforcementAction) {
        appointmentRepository.save(
            // This isn't really an appointment - but enforcement action needs a corresponding contact
            AppointmentContact(
                linkedContact = this,
                type = action.type,
                date = LocalDate.now(),
                startTime = ZonedDateTime.now(),
                personId = personId,
                eventId = eventId,
                nsiId = nsiId,
                requirementId = requirementId,
                licenceConditionId = licenceConditionId,
                pssRequirementId = pssRequirementId,
                provider = provider,
                team = team,
                staff = staff,
                officeLocation = officeLocation,
            )
        )
    }

    private fun AppointmentContact.createEnforcementReviewContact(reviewType: Type) {
        appointmentRepository.save(
            AppointmentContact(
                linkedContact = this,
                type = reviewType,
                date = LocalDate.now(),
                startTime = ZonedDateTime.now(),
                personId = personId,
                eventId = eventId,
                nsiId = nsiId,
                requirementId = requirementId,
                licenceConditionId = licenceConditionId,
                pssRequirementId = pssRequirementId,
                provider = provider,
                team = team,
                staff = staff,
                officeLocation = officeLocation,
            )
        )
    }

    private fun AppointmentContact.event() = event ?: eventId?.let { eventRepository.findByIdOrNull(it) }
}