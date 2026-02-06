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

    fun applyEnforcementAction(appointment: AppointmentContact, action: EnforcementAction, reviewType: Type) {
        appointment.applyEnforcementAction(action)
        appointment.updateFailureToComplyCount(reviewType)
    }

    fun AppointmentContact.applyEnforcementAction(action: EnforcementAction) {
        enforcementActionId = action.id
        enforcement = true

        if (id == null || !enforcementRepository.existsByContactId(id)) {
            enforcementRepository.save(
                Enforcement(
                    contact = this,
                    action = action,
                    responseDate = action.responseByPeriod?.let { ZonedDateTime.now().plusDays(it) }
                )
            )
            createEnforcementActionContact(action)
        }
    }

    private fun AppointmentContact.updateFailureToComplyCount(reviewType: Type) {
        val event = event ?: eventId?.let { eventRepository.findByIdOrNull(it) } ?: return
        appointmentRepository.save(this)
        event.ftcCount = appointmentRepository.countFailureToComply(event)

        val ftcLimit = event.disposal?.type?.ftcLimit ?: return
        if (event.ftcCount > ftcLimit && !appointmentRepository.enforcementReviewExists(event.id, event.breachEnd)) {
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
                notes = listOfNotNull(
                    notes,
                    """
                        ${DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now())}
                        Enforcement Action: ${action.description}
                    """.trimIndent()
                ).joinToString("\n\n"),
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
}