package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.api.model.MergeAppointment
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.exception.AppointmentNotFoundException
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.asReason
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.ADD_CONTACT
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode.UPDATE_CONTACT
import uk.gov.justice.digital.hmpps.integrations.delius.contact.*
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactOutcome.Code
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.Enforcement
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.EnforcementAction
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.entity.Nsi
import uk.gov.justice.digital.hmpps.messaging.Referral
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime.now
import java.util.*

@Service
class AppointmentService(
    auditedInteractionService: AuditedInteractionService,
    private val providerService: ProviderService,
    private val contactRepository: ContactRepository,
    private val contactTypeRepository: ContactTypeRepository,
    private val outcomeRepository: ContactOutcomeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val enforcementRepository: EnforcementRepository,
    private val eventRepository: EventRepository,
    private val nsiRepository: NsiRepository,
    private val telemetryService: TelemetryService // temporarily added here for determining fuzzy matches
) : AuditableService(auditedInteractionService) {

    @Transactional
    fun mergeAppointment(crn: String, mergeAppointment: MergeAppointment): Long = audit(ADD_CONTACT) { audit ->
        val nsi = nsiRepository.findByPersonCrnAndExternalReference(crn, mergeAppointment.referralUrn)
            ?: throw NotFoundException(
                "Unable to match Referral ${mergeAppointment.referralUrn} => CRN $crn"
            )
        audit["offenderId"] = nsi.person.id

        if (now().isAfter(mergeAppointment.start) && mergeAppointment.outcome == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Appointment started in the past and no outcome was provided"
            )
        }
        checkForConflicts(nsi.person.id, mergeAppointment)

        val assignation = providerService.findCrsAssignationDetails(mergeAppointment.officeLocationCode)
        val find = {
            contactRepository.findByPersonCrnAndExternalReference(
                crn,
                mergeAppointment.previousUrn ?: mergeAppointment.urn
            )
        }
        val appointment = find() ?: mergeAppointment.deliusId?.let {
            val app = contactRepository.findByIdOrNull(it)
            if (app != null) {
                telemetryService.trackEvent(
                    "Appointment Found By Delius Id - Merge Appointment",
                    mapOf(
                        "crn" to crn,
                        "urn" to mergeAppointment.urn,
                        "deliusId" to mergeAppointment.deliusId.toString(),
                        "referralReference" to mergeAppointment.referralReference,
                        "referralUrn" to mergeAppointment.referralUrn
                    )
                )
            }
            app
        } ?: createContact(mergeAppointment, assignation, nsi)

        audit["contactId"] = appointment.id
        appointment.locationId = assignation.location?.id

        mergeAppointment.notes?.also {
            if (appointment.notes?.contains(it) != true) appointment.addNotes(mergeAppointment.notes)
        }

        val replacement = appointment.replaceIfRescheduled(
            mergeAppointment.urn,
            mergeAppointment.start,
            mergeAppointment.end,
            mergeAppointment.countsTowardsRar
        )?.addNotes(mergeAppointment.notes)
        replacement?.also {
            appointment.outcome = outcomeRepository.getByCode(Code.RESCHEDULED_SERVICE_REQUEST.value)
            appointment.rarActivity = false
            contactRepository.save(it)
        }

        mergeAppointment.outcome?.applyTo(replacement ?: appointment) ?: replacement?.id ?: appointment.id
    }

    fun Outcome.applyTo(appointment: Contact): Long {
        val outcome = outcomeRepository.getByCode(attendanceOutcome(this).value)
        appointment.outcome = outcome

        if (!sessionHappened) {
            appointment.rarActivity = false
        }

        if (outcome.compliantAcceptable == false) {
            handleNonCompliance(appointment)
        }

        contactRepository.saveAndFlush(appointment)
        nsiRepository.findByIdIfRar(appointment.nsiId!!)?.rarCount = contactRepository.countNsiRar(appointment.nsiId)
        return appointment.id
    }

    private fun createContact(mergeAppointment: MergeAppointment, assignation: CrsAssignation, nsi: Nsi): Contact =
        contactRepository.save(
            Contact(
                nsi.person,
                contactTypeRepository.getByCode(mergeAppointment.typeCode.value),
                providerId = assignation.provider.id,
                teamId = assignation.team.id,
                staffId = assignation.staff.id,
                locationId = assignation.location?.id,
                eventId = nsi.eventId,
                nsiId = nsi.id,
                rarActivity = mergeAppointment.countsTowardsRar && nsiRepository.isRar(nsi.id) == true,
                externalReference = mergeAppointment.urn,
                date = mergeAppointment.start.toLocalDate(),
                startTime = mergeAppointment.start,
                endTime = mergeAppointment.end
            )
        )

    private fun checkForConflicts(
        personId: Long,
        mergeAppointment: MergeAppointment
    ) {
        if (mergeAppointment.start.isAfter(now()) && contactRepository.appointmentClashes(
                personId,
                mergeAppointment.urn,
                mergeAppointment.start.toLocalDate(),
                mergeAppointment.start,
                mergeAppointment.end,
                mergeAppointment.previousUrn
            )
        ) {
            throw ConflictException("Appointment conflicts with an existing future appointment")
        }
    }

    @Transactional
    fun updateOutcome(uao: UpdateAppointmentOutcome): Unit = audit(UPDATE_CONTACT) { audit ->
        audit["contactId"] = uao.id

        val appointment = contactRepository.findByPersonCrnAndExternalReference(uao.crn, uao.urn)
            ?: uao.deliusId?.let {
                val app = contactRepository.findByIdOrNull(it)
                if (app != null) {
                    telemetryService.trackEvent(
                        "Appointment Found By Delius Id - Feedback Submitted",
                        mapOf(
                            "crn" to uao.crn,
                            "urn" to uao.urn,
                            "deliusId" to uao.deliusId.toString(),
                            "referralReference" to uao.referralReference,
                            "referralUrn" to uao.referral.urn
                        )
                    )
                }
                app
            }
            ?: throw AppointmentNotFoundException(
                appointmentId = uao.id,
                deliusId = uao.deliusId,
                referralReference = uao.referralReference,
                outcome = uao.outcome,
                reason = contactRepository.getNotFoundReason(uao.crn, uao.referral.urn, uao.urn, uao.deliusId ?: -1)
                    .asReason()
            )

        if (appointment.notes?.contains(uao.notes) != true) {
            appointment.addNotes(uao.notes)
        }

        uao.outcome.applyTo(appointment)
    }

    private fun handleNonCompliance(appointment: Contact) {
        val action = enforcementActionRepository.getByCode(EnforcementAction.Code.REFER_TO_PERSON_MANAGER.value)
        enforcementRepository.findByContactId(appointment.id) ?: Enforcement(
            appointment,
            action,
            action.responseByPeriod?.let { now().plusDays(it) }
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
            if (event.disposal?.type?.overLimit(currentCount) == true && !event.enforcementUnderReview()) {
                contactRepository.save(appointment.reviewEnforcement())
            }
        }
    }

    private fun Contact.createEnforcementActionContact(action: EnforcementAction) = Contact(
        person = person,
        type = action.contactType,
        date = LocalDate.now(),
        startTime = now(),
        eventId = eventId,
        nsiId = nsiId,
        providerId = providerId,
        teamId = teamId,
        staffId = staffId,
        locationId = locationId,
        linkedContactId = id
    ).addNotes("${notes}\n${LocalDateTime.now()}\nEnforcement Action: ${action.description}")

    private fun Event.enforcementUnderReview() = contactRepository.countEnforcementUnderReview(
        id,
        ContactType.Code.REVIEW_ENFORCEMENT_STATUS.value,
        breachEnd
    ) > 0

    private fun Contact.reviewEnforcement() = Contact(
        person,
        contactTypeRepository.getByCode(ContactType.Code.REVIEW_ENFORCEMENT_STATUS.value),
        date,
        startTime,
        endTime,
        eventId = eventId,
        nsiId = nsiId,
        providerId = providerId,
        teamId = teamId,
        staffId = staffId,
        locationId = locationId,
        linkedContactId = id
    )

    companion object {
        private fun attendanceOutcome(outcome: Outcome): Code =
            when (outcome.sessionHappened) {
                true -> if (outcome.notify) Code.FAILED_TO_COMPLY else Code.COMPLIED
                false -> when (outcome.attended) {
                    Attended.NO -> Code.FAILED_TO_ATTEND
                    else -> noSession(outcome)
                }
            }

        private fun noSession(outcome: Outcome) = when (outcome.noSessionReasonType) {
            NoSessionReasonType.POP_UNACCEPTABLE -> Code.FAILED_TO_COMPLY
            NoSessionReasonType.POP_ACCEPTABLE -> Code.APPOINTMENT_KEPT
            NoSessionReasonType.LOGISTICS -> Code.SENT_HOME
            else -> throw IllegalArgumentException("Outcome Scenario Not Mapped: $outcome")
        }
    }
}

data class UpdateAppointmentOutcome(
    val id: UUID,
    val deliusId: Long?,
    val crn: String,
    val referralReference: String,
    val referral: Referral,
    val outcome: Outcome,
    val url: String
) {
    val notes =
        "Session Feedback Recorded for ${referral.contractType} Referral $referralReference with Prime Provider ${referral.provider.name}${System.lineSeparator()}$url"
    val urn
        get() = "urn:hmpps:interventions-appointment:$id"
}

enum class Attended {
    YES, LATE, NO;

    companion object {
        fun of(value: String): Attended = entries.first { it.name.lowercase() == value.lowercase() }
    }
}

data class Outcome(
    val attended: Attended,
    private val didSessionHappen: Boolean? = null,
    val noSessionReasonType: NoSessionReasonType? = null,
    @JsonAlias("notifyProbationPractitionerOfBehaviour")
    val notify: Boolean = false
) {
    val sessionHappened = didSessionHappen ?: false
}

enum class NoSessionReasonType {
    LOGISTICS,
    POP_ACCEPTABLE,
    POP_UNACCEPTABLE
}
