package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.domain.contact.Contact
import uk.gov.justice.digital.hmpps.appointments.domain.contact.AppointmentRepository
import uk.gov.justice.digital.hmpps.appointments.domain.contact.ContactType
import uk.gov.justice.digital.hmpps.appointments.domain.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.appointments.domain.contact.Enforcement
import uk.gov.justice.digital.hmpps.appointments.domain.contact.EnforcementAction
import uk.gov.justice.digital.hmpps.appointments.domain.contact.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.appointments.domain.contact.EnforcementRepository
import uk.gov.justice.digital.hmpps.appointments.domain.contact.getEnforcementActionByCode
import uk.gov.justice.digital.hmpps.appointments.domain.contact.getTypeByCode
import uk.gov.justice.digital.hmpps.appointments.domain.event.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime.now

@Transactional
@Service
class EnforcementService(
    private val enforcementActionRepository: EnforcementActionRepository,
    private val enforcementRepository: EnforcementRepository,
    private val appointmentRepository: AppointmentRepository,
    private val contactTypeRepository: ContactTypeRepository,
) {
    fun enforceIfNonCompliant(appointment: Contact) {
        if (appointment.outcome?.acceptable != false || appointment.outcome?.enforceable != true) {
            return
        }

        val action =
            enforcementActionRepository.getEnforcementActionByCode(EnforcementAction.Code.REFER_TO_PERSON_MANAGER.value)
        enforcementRepository.findByContactId(appointment.id) ?: Enforcement(
            appointment,
            action,
            action.responseByPeriod?.let { now().plusDays(it) }
        ).apply {
            enforcementRepository.save(this)
            val eac = appointment.createEnforcementActionContact(action)
            appointmentRepository.save(eac)
        }
        appointment.enforcementActionId = action.id
        appointment.enforcement = true

        appointment.event?.let { event ->
            val currentCount = appointmentRepository.countFailureToComply(
                event.id,
                listOfNotNull(event.breachEnd, event.disposal?.date).maxOrNull()
            )
            event.ftcCount = currentCount
            if (event.disposal?.type?.overLimit(currentCount) == true && !event.enforcementUnderReview()) {
                appointmentRepository.save(appointment.reviewEnforcement())
            }
        }
    }

    private fun Contact.createEnforcementActionContact(action: EnforcementAction) = Contact(
        person = person,
        event = event,
        requirement = requirement,
        type = action.type,
        date = LocalDate.now(),
        startTime = now(),
        endTime = null,
        notes = notes,
        provider = provider,
        team = team,
        staff = staff,
        location = location,
        sensitive = null,
        rarActivity = null,
        sendToVisor = null,
        outcome = null,
        externalReference = null
    ).linkTo(this)
        .appendNotes("${LocalDateTime.now()}" + System.lineSeparator() + "Enforcement Action: ${action.description}")

    private fun Event.enforcementUnderReview() = appointmentRepository.countEnforcementUnderReview(
        id,
        ContactType.Code.REVIEW_ENFORCEMENT_STATUS.value,
        breachEnd
    ) > 0

    private fun Contact.reviewEnforcement() = Contact(
        person,
        event,
        requirement,
        contactTypeRepository.getTypeByCode(ContactType.Code.REVIEW_ENFORCEMENT_STATUS.value),
        date,
        startTime,
        endTime,
        provider = provider,
        team = team,
        staff = staff,
        location = location,
        notes = null,
        sensitive = null,
        rarActivity = null,
        sendToVisor = null,
        outcome = null,
        externalReference = null
    ).linkTo(this)
}