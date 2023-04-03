package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.UPDATE_CONTACT
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactOutcomeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.EnforcementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome.Code
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.contact.getAppointmentById
import uk.gov.justice.digital.hmpps.integrations.delius.contact.getByCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.messaging.Referral
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Service
class AppointmentService(
    auditedInteractionService: AuditedInteractionService,
    private val contactRepository: ContactRepository,
    private val outcomeRepository: ContactOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val enforcementRepository: EnforcementRepository,
    private val eventRepository: EventRepository,
    private val nsiRepository: NsiRepository
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun updateOutcome(uao: UpdateAppointmentOutcome): Unit = audit(UPDATE_CONTACT) { audit ->
        audit["contactId"] = uao.id

        val appointment = contactRepository.getAppointmentById(uao.id)
        val outcome = outcomeRepository.getByCode(attendanceOutcome(uao).value)
        appointment.outcome = outcome
        if (appointment.notes?.contains(uao.notes) != true) {
            appointment.notes += """${System.lineSeparator()}
            |----------
            |
            |${uao.notes}
            """.trimMargin()
        }

        appointment.hoursCredited = if (outcome.code == Code.COMPLIED.value) {
            BigDecimal.valueOf(appointment.duration.toMinutes())
                .divide(BigDecimal(60), 2, RoundingMode.HALF_UP)
                .toDouble()
        } else {
            null
        }

        if (uao.notify && outcome.compliantAcceptable == false) {
            handleNonCompliance(appointment)
        } else if (outcome.compliantAcceptable == true) {
            nsiRepository.findByIdIfRar(appointment.nsiId!!)?.rarCount =
                contactRepository.countNsiRar(appointment.nsiId)
        }
    }

    private fun handleNonCompliance(appointment: Contact) {
        val action = enforcementActionRepository.getByCode(EnforcementAction.Code.REFER_TO_PERSON_MANAGER.value)
        enforcementRepository.findByContactId(appointment.id) ?: Enforcement(
            appointment,
            action,
            action.responseByPeriod?.let { ZonedDateTime.now().plusDays(it) }
        ).apply {
            enforcementRepository.save(this)
            val eac = appointment.createEnforcementActionContact(action)
            contactRepository.save(eac)
        }
        appointment.enforcementActionId = action.id
        appointment.enforcement = true

        appointment.eventId?.let {
            val event = eventRepository.findById(it).orElseThrow { NotFoundException("Event", "id", it) }
            val currentCount = contactRepository.countFailureToComply(
                it,
                listOfNotNull(event.breachEnd, event.disposal?.date).maxOrNull()
            )
            event.ftcCount = currentCount
            if (event.disposal?.type?.overLimit(currentCount) == true) {
                // TODO check if enforcement is under review
                // if not - create REVIEW_ENFORCEMENT_STATUS("ARWS") contact
            }
        }
    }

    private fun Contact.createEnforcementActionContact(action: EnforcementAction) = Contact(
        person = person,
        type = action.contactType,
        date = date,
        startTime = startTime,
        endTime = endTime,
        eventId = eventId,
        nsiId = nsiId,
        notes = "${notes}\n${LocalDateTime.now()}\nEnforcement Action: ${action.description}",
        providerId = providerId,
        teamId = teamId,
        staffId = staffId,
        locationId = locationId
    )

    companion object {
        private fun attendanceOutcome(uoa: UpdateAppointmentOutcome): Code =
            when (uoa.attended) {
                Attended.YES, Attended.LATE -> if (uoa.notify) Code.FAILED_TO_COMPLY else Code.COMPLIED
                Attended.NO -> Code.FAILED_TO_ATTEND
            }
    }
}

data class UpdateAppointmentOutcome(
    val id: Long,
    val crn: String,
    val referralReference: String,
    val referral: Referral,
    val attended: Attended,
    val notify: Boolean,
    val url: String
) {
    val notes =
        "Session Feedback Submitted for ${referral.contractType} Referral $referralReference with Prime Provider ${referral.provider.name}${System.lineSeparator()}$url"
}

enum class Attended {
    YES, LATE, NO;

    companion object {
        fun of(value: String): Attended = values().first { it.name.lowercase() == value.lowercase() }
    }
}
