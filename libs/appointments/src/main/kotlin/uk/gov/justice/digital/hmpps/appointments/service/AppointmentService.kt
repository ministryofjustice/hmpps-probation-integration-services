package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentOutcome
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.EnforcementAction
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type
import uk.gov.justice.digital.hmpps.appointments.model.*
import uk.gov.justice.digital.hmpps.appointments.model.UpdateAppointment.*
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.LocationRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.OutcomeRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.StaffRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.TeamRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.TypeRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.getAllByCodeIn
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.getByCode
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.AppointmentRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.PersonRepository
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import java.time.ZonedDateTime

@Service
@Transactional
class AppointmentService internal constructor(
    private val appointmentRepository: AppointmentRepository,
    private val personRepository: PersonRepository,
    private val teamRepository: TeamRepository,
    private val staffRepository: StaffRepository,
    private val locationRepository: LocationRepository,
    private val typeRepository: TypeRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val outcomeRepository: OutcomeRepository,
    private val enforcementService: EnforcementService,
    private val alertService: AlertService,
    auditedInteractionService: AuditedInteractionService,
) : AuditableService(auditedInteractionService) {

    fun create(request: CreateAppointment): Appointment = bulkCreate(listOf(request)).single()

    fun bulkCreate(requests: List<CreateAppointment>) = createEntities(requests).map { Appointment(it) }

    private fun createEntities(requests: List<CreateAppointment>): List<AppointmentContact> = with(requests) {
        val types = typeRepository.getAllByCodeIn(map { it.typeCode })
        val outcomes = outcomeRepository.getAllByCodeIn(mapNotNull { it.outcomeCode })
        val enforcementAction = enforcementActionRepository.getByCode(EnforcementAction.REFER_TO_PERSON_MANAGER)
        val enforcementReviewType = typeRepository.getByCode(Type.REVIEW_ENFORCEMENT_STATUS)
        val locations = locationRepository.getAllByCodeIn(mapNotNull { it.locationCode })
        val teams = teamRepository.getAllByCodeIn(map { it.teamCode })
        val staff = staffRepository.getAllByCodeIn(map { it.staffCode })

        appointmentRepository
            .saveAll(map { request ->
                AppointmentContact(
                    externalReference = request.reference,
                    personId = request.relatedTo.personId
                        ?: personRepository.getIdByCrn(requireNotNull(request.relatedTo.crn) { "Either personId or crn must be provided" }),
                    eventId = request.relatedTo.eventId,
                    nsiId = request.relatedTo.nonStatutoryInterventionId,
                    licenceConditionId = request.relatedTo.licenceConditionId,
                    requirementId = request.relatedTo.requirementId,
                    pssRequirementId = request.relatedTo.pssRequirementId,
                    date = request.date,
                    startTime = ZonedDateTime.of(request.date, request.startTime, EuropeLondon),
                    endTime = ZonedDateTime.of(request.date, request.endTime, EuropeLondon),
                    team = teams[request.teamCode].orNotFoundBy("code", request.teamCode),
                    staff = staff[request.staffCode].orNotFoundBy("code", request.staffCode),
                    officeLocation = request.locationCode?.let { code -> locations[code].orNotFoundBy("code", code) },
                    type = types[request.typeCode].orNotFoundBy("code", request.typeCode),
                    notes = request.notes,
                    sensitive = request.sensitive,
                    visorContact = request.exportToVisor,
                ).checkForSchedulingConflicts(
                    allowConflicts = request.allowConflicts
                ).applyOutcome(
                    outcome = request.outcomeCode?.let { code -> outcomes[code].orNotFoundBy("code", code) },
                    enforcementAction = enforcementAction,
                    enforcementReviewType = enforcementReviewType,
                )
            })
            .onEach { it.audit(BusinessInteractionCode.ADD_CONTACT) }
    }

    fun <T> update(request: T, init: UpdateBuilder<T>.() -> Unit) = bulkUpdate(listOf(request), init).single()

    fun <T> bulkUpdate(requests: List<T>, init: UpdateBuilder<T>.() -> Unit): List<Appointment> {
        val updates = UpdateBuilder<T>().also { it.init() }
        return requests
            .withExisting(updates.reference, updates.id)
            .validateUpdates(updates)
            .applyUpdates(updates.amendDateTime) { amendDateTime(it) }
            .applyUpdates(updates.recreate) { recreate(it) }
            .applyUpdates(updates.reschedule) { reschedule(it) }
            .applyUpdates(updates.reassign) { reassign(it) }
            .applyUpdates(updates.applyOutcome) { applyOutcome(it) }
            .applyUpdates(updates.appendNotes) { appendNotes(it) }
            .applyUpdates(updates.flagAs) { flagAs(it) }
            .map { (_, contact) -> contact }
            .onEach { it.audit(BusinessInteractionCode.UPDATE_CONTACT) }
            .map { Appointment(it) }
    }

    private fun <T> List<T>.withExisting(
        reference: ValueProvider<T, String>?,
        id: ValueProvider<T, Long>?
    ): UpdatePipeline<T> = when {
        reference != null -> map { reference.invoke(it) }.toSet().let { request ->
            request.chunked(500)
                .flatMap { chunk -> appointmentRepository.findByExternalReferenceIn(chunk) }
                .also { contacts ->
                    val missing = request - contacts.mapNotNull { it.externalReference }.toSet()
                    require(missing.isEmpty()) { "Could not find appointments with references: $missing" }
                }
                .mapNotNull { contact -> find { reference.invoke(it) == contact.externalReference }?.let { it to contact } }
        }

        id != null -> map { id.invoke(it) }.toSet().let { request ->
            request
                .chunked(500)
                .flatMap { chunk -> appointmentRepository.findByIdIn(chunk) }
                .also { contacts ->
                    val missing = request - contacts.mapNotNull { it.id }.toSet()
                    require(missing.isEmpty()) { "Could not find appointments with IDs: $missing" }
                }
                .mapNotNull { contact -> find { id.invoke(it) == contact.id }?.let { it to contact } }
        }

        else -> requireNotNull(reference ?: id) { "Reference must be provided" }
    }

    private fun <T> UpdatePipeline<T>.validateUpdates(updates: UpdateBuilder<T>) = onEach { (request, existing) ->
        val outcome = updates.applyOutcome?.invoke(Outcome(existing), request)?.outcomeCode
        val amendDateTime = updates.amendDateTime?.invoke(Schedule(existing), request)
        require(amendDateTime == null || amendDateTime.isFuture || outcome != null) {
            "Outcome must be provided when amending an appointment in the past"
        }
    }

    private fun <T> UpdatePipeline<T>.amendDateTime(applyUpdates: UpdateProvider<T, Schedule>) =
        map { (request, existing) ->
            val update = Schedule(existing).applyUpdates(request)
            request to existing
                .amendDateTime(update)
                .checkForSchedulingConflicts(update.allowConflicts)
        }

    private fun AppointmentContact.amendDateTime(request: Schedule): AppointmentContact {
        if (request isSameDateAndTimeAs this) return this

        require(outcome == null) {
            "Appointment with outcome cannot be rescheduled"
        }

        return apply {
            date = request.date
            startTime = request.date.atTime(request.startTime).atZone(EuropeLondon)
            endTime = request.endTime?.let { request.date.atTime(it).atZone(EuropeLondon) }
        }
    }

    private fun <T> UpdatePipeline<T>.recreate(applyUpdates: UpdateProvider<T, Recreate>): UpdatePipeline<T> {
        val updates = associate { (request, existing) -> request to Recreate(existing).applyUpdates(request) }
        val outcomeCodes = updates.mapNotNull { (_, update) -> update.rescheduledBy?.outcomeCode }
        val outcomes = outcomeRepository.getAllByCodeIn(outcomeCodes)

        return map { (request, existing) ->
            val update = updates.getValue(request)
            val outcome = update.rescheduledBy?.outcomeCode?.let { outcomes[it] }
            request to existing
                .recreate(update, outcome)
                .checkForSchedulingConflicts(update.allowConflicts)
        }
    }

    private fun AppointmentContact.recreate(
        request: Recreate,
        rescheduleOutcome: AppointmentOutcome?
    ): AppointmentContact {
        if (request isSameDateAndTimeAs this) return this

        require(outcome == null) {
            "Appointment with outcome cannot be rescheduled"
        }

        requireNotNull(rescheduleOutcome) {
            "Recreating an appointment requires a rescheduling reason (service request or person-on-probation request)"
        }

        requireNotNull(request.newReference) {
            "A new reference must be provided when recreating an appointment"
        }

        val requestDate = request.date.atTime(request.startTime).atZone(EuropeLondon)
        require(requestDate >= ZonedDateTime.now()) {
            "Appointment cannot be rescheduled into the past"
        }

        // Update the existing appointment with the rescheduled outcome
        outcome = rescheduleOutcome

        // Create replacement appointment
        return createEntities(
            listOf(
                CreateAppointment(
                    reference = request.newReference,
                    date = request.date,
                    startTime = request.startTime,
                    endTime = request.endTime,
                    typeCode = type.code,
                    relatedTo = ReferencedEntities(
                        personId = personId,
                        eventId = eventId,
                        nonStatutoryInterventionId = nsiId,
                        licenceConditionId = licenceConditionId,
                        requirementId = requirementId,
                        pssRequirementId = pssRequirementId
                    ),
                    staffCode = staff.code,
                    teamCode = team.code,
                    locationCode = officeLocation?.code,
                    outcomeCode = null,
                    notes = notes,
                    sensitive = sensitive,
                    exportToVisor = visorExported,
                )
            )
        ).single()
    }

    /**
     * Helper - decides whether to recreate or amend the appointment based on the existing appointment date.
     */
    private fun <T> UpdatePipeline<T>.reschedule(applyUpdates: UpdateProvider<T, Recreate>): UpdatePipeline<T> {
        val updates = associate { (request, existing) -> request to Recreate(existing).applyUpdates(request) }
        val outcomeCodes = updates.mapNotNull { (_, update) -> update.rescheduledBy?.outcomeCode }
        val outcomes = outcomeRepository.getAllByCodeIn(outcomeCodes)

        return map { (request, existing) ->
            val update = updates.getValue(request)
            val existingDate = existing.date.atTime(existing.startTime.toLocalTime()).atZone(EuropeLondon)
            request to if (existingDate <= ZonedDateTime.now()) {
                val outcome = update.rescheduledBy?.outcomeCode?.let { outcomes[it].orNotFoundBy("code", it) }
                existing.recreate(update, outcome)
            } else {
                existing.amendDateTime(Schedule(update))
            }
        }
    }

    private fun <T> UpdatePipeline<T>.applyOutcome(applyUpdates: UpdateProvider<T, Outcome>): UpdatePipeline<T> {
        val updates = associate { (request, existing) -> request to Outcome(existing).applyUpdates(request) }
        val outcomes = outcomeRepository.getAllByCodeIn(updates.mapNotNull { (_, update) -> update.outcomeCode })
        val enforcementAction = enforcementActionRepository.getByCode(EnforcementAction.REFER_TO_PERSON_MANAGER)
        val enforcementReviewType = typeRepository.getByCode(Type.REVIEW_ENFORCEMENT_STATUS)

        return map { (request, existing) ->
            val update = updates.getValue(request)
            request to existing.applyOutcome(
                outcome = update.outcomeCode?.let { code -> outcomes[code].orNotFoundBy("code", code) },
                enforcementAction = enforcementAction,
                enforcementReviewType = enforcementReviewType,
            )
        }
    }

    private fun AppointmentContact.applyOutcome(
        outcome: AppointmentOutcome?,
        enforcementAction: EnforcementAction,
        enforcementReviewType: Type
    ) = apply {
        this.outcome = outcome
        if (outcome != null) {
            attended = outcome.attended
            complied = outcome.complied
            if (outcome.complied == false && outcome.enforceable == true) {
                enforcementService.applyEnforcementAction(this, enforcementAction, enforcementReviewType)
            }
        }
    }

    private fun <T> UpdatePipeline<T>.reassign(applyUpdates: UpdateProvider<T, Assignee>): UpdatePipeline<T> {
        val updates = associate { (request, existing) -> request to Assignee(existing).applyUpdates(request) }
        val allStaff = staffRepository.getAllByCodeIn(updates.map { (_, update) -> update.staffCode })
        val allTeams = teamRepository.getAllByCodeIn(updates.map { (_, update) -> update.teamCode })
        val allLocations = locationRepository.getAllByCodeIn(updates.mapNotNull { (_, update) -> update.locationCode })

        return map { (request, existing) ->
            request to existing.apply {
                val update = updates.getValue(request)
                staff = allStaff[update.staffCode].orNotFoundBy("code", update.staffCode)
                team = allTeams[update.teamCode].orNotFoundBy("code", update.teamCode)
                officeLocation = update.locationCode?.let { allLocations[it].orNotFoundBy("code", it) }
            }
        }
    }

    private fun <T> UpdatePipeline<T>.appendNotes(applyUpdates: UpdateProvider<T, String?>): UpdatePipeline<T> {
        return map { (request, existing) ->
            request to existing.apply {
                notes = listOfNotNull(notes, notes.applyUpdates(request)).joinToString("\n\n")
            }
        }
    }

    private fun <T> UpdatePipeline<T>.flagAs(applyUpdates: UpdateProvider<T, Flags>) = map { (request, existing) ->
        val update = Flags(existing).applyUpdates(request)
        request to existing.apply {
            alert = update.alert?.also { alert ->
                if (alert) alertService.createAlert(this)
                else alertService.removeAlert(this)
            }
            sensitive = update.sensitive
            rarActivity = update.rarActivity
            visorContact = update.visor
            visorExported = if (visorContact == true) visorExported ?: false else null
        }
    }

    internal fun AppointmentContact.checkForSchedulingConflicts(allowConflicts: Boolean) = also {
        require(allowConflicts || !appointmentRepository.schedulingConflictExists(this)) {
            "Appointment with reference $externalReference conflicts with an existing appointment"
        }
    }

    private fun AppointmentContact.audit(interactionCode: BusinessInteractionCode) = audit(interactionCode) { audit ->
        audit["offenderId"] = personId
        audit["contactId"] = id!!
    }
}
