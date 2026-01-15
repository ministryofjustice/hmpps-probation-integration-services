package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.EnforcementAction
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Outcome
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type
import uk.gov.justice.digital.hmpps.appointments.model.*
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
import java.time.temporal.ChronoUnit

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
    auditedInteractionService: AuditedInteractionService,
) : AuditableService(auditedInteractionService) {

    fun create(request: CreateAppointment): Appointment = create(listOf(request)).single()

    fun create(requests: List<CreateAppointment>) = createEntities(requests).map { Appointment(it) }

    private fun createEntities(requests: List<CreateAppointment>): List<AppointmentContact> = with(requests) {
        val types = typeRepository.getAllByCodeIn(map { it.typeCode })
        val outcomes = outcomeRepository.getAllByCodeIn(mapNotNull { it.outcomeCode })
        val enforcementAction = enforcementActionRepository.getByCode(EnforcementAction.REFER_TO_PERSON_MANAGER)
        val enforcementReviewType = typeRepository.getByCode(Type.REVIEW_ENFORCEMENT_STATUS)
        val locations = locationRepository.getAllByCodeIn(mapNotNull { it.locationCode })
        val teams = teamRepository.getAllByCodeIn(map { it.teamCode })
        val staff = staffRepository.getAllByCodeIn(map { it.staffCode })

        val entities = map { request ->
            val team = teams[request.teamCode].orNotFoundBy("code", request.teamCode)
            val staffMember = staff[request.staffCode].orNotFoundBy("code", request.staffCode)

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
                provider = team.provider,
                team = team,
                staff = staffMember,
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
        }

        appointmentRepository.saveAll(entities)
            .onEach { it.audit(BusinessInteractionCode.ADD_CONTACT) }
    }

    fun <T> update(requests: List<T>, init: UpdateBuilder<T>.() -> Unit): List<Appointment> {
        val config = UpdateBuilder<T>().also { it.init() }
        fun T.reference() = requireNotNull(config.reference) { "Reference must be provided" }.invoke(this)

        return requests
            .map { it.reference() }
            .chunked(500)
            .flatMap { chunk -> appointmentRepository.findByExternalReferenceIn(chunk) }
            .mapNotNull { contact ->
                requests.find { it.reference() == contact.externalReference }?.let { it to contact }
            }
            .withConfig(config.amendDateTime) { amendDateTime(it) }
            .withConfig(config.recreate) { recreate(it) }
            .withConfig(config.reschedule) { reschedule(it) }
            .withConfig(config.reassign) { reassign(it) }
            .withConfig(config.applyOutcome) { applyOutcome(it) }
            .withConfig(config.appendNotes) { appendNotes(it) }
            .withConfig(config.flagAs) { flagAs(it) }
            .map { (_, contact) -> contact }
            .onEach { it.audit(BusinessInteractionCode.UPDATE_CONTACT) }
            .map { Appointment(it) }
    }

    private fun <T> UpdatePipeline<T>.amendDateTime(scheduleProvider: (T) -> UpdateAppointment.Schedule): UpdatePipeline<T> {
        fun T.schedule() = scheduleProvider.invoke(this)
        return map { (request, existing) ->
            request to existing
                .amendDateTime(request.schedule())
                .checkForSchedulingConflicts(request.schedule().allowConflicts)
        }
    }

    private fun AppointmentContact.amendDateTime(request: UpdateAppointment.Schedule): AppointmentContact {
        val existingDate = date.atTime(startTime.toLocalTime()).atZone(EuropeLondon)
        val requestDate = request.date.atTime(request.startTime).atZone(EuropeLondon)
        if (requestDate.truncatedTo(ChronoUnit.SECONDS) == existingDate.truncatedTo(ChronoUnit.SECONDS)) return this

        require(outcome == null) {
            "Appointment with outcome cannot be rescheduled"
        }

        require(existingDate >= ZonedDateTime.now()) {
            "Appointment must be in the future to amend the date and time"
        }

        require(requestDate >= ZonedDateTime.now()) {
            "Appointment cannot be rescheduled into the past"
        }

        return apply {
            date = request.date
            startTime = request.date.atTime(request.startTime).atZone(EuropeLondon)
            endTime = request.endTime?.let { request.date.atTime(it).atZone(EuropeLondon) }
        }
    }

    private fun <T> UpdatePipeline<T>.recreate(scheduleProvider: (T) -> UpdateAppointment.RecreateAppointment): UpdatePipeline<T> {
        fun T.schedule() = scheduleProvider.invoke(this)
        val outcomeCodes = mapNotNull { (request, _) -> request.schedule().rescheduledBy?.outcomeCode }
        val outcomes = outcomeRepository.getAllByCodeIn(outcomeCodes)

        return map { (request, existing) ->
            val schedule = request.schedule()
            val outcome = schedule.rescheduledBy?.outcomeCode?.let { outcomes[it] }
            request to existing
                .recreate(schedule, outcome)
                .checkForSchedulingConflicts(request.schedule().allowConflicts)
        }
    }

    private fun AppointmentContact.recreate(
        request: UpdateAppointment.RecreateAppointment,
        rescheduleOutcome: Outcome?
    ): AppointmentContact {
        val existingDate = date.atTime(startTime.toLocalTime()).atZone(EuropeLondon)
        val requestDate = request.date.atTime(request.startTime).atZone(EuropeLondon)
        if (requestDate.truncatedTo(ChronoUnit.SECONDS) == existingDate.truncatedTo(ChronoUnit.SECONDS)) return this

        require(outcome == null) {
            "Appointment with outcome cannot be rescheduled"
        }

        requireNotNull(rescheduleOutcome) {
            "Recreating an appointment requires a rescheduling reason (service request or person-on-probation request)"
        }

        requireNotNull(request.newReference) {
            "A new reference must be provided when recreating an appointment"
        }

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
    private fun <T> UpdatePipeline<T>.reschedule(scheduleProvider: (T) -> UpdateAppointment.RecreateAppointment): UpdatePipeline<T> {
        fun T.schedule() = scheduleProvider.invoke(this)
        val outcomeCodes = mapNotNull { (request, _) -> request.schedule().rescheduledBy?.outcomeCode }
        val outcomes = outcomeRepository.getAllByCodeIn(outcomeCodes)

        return map { (request, existing) ->
            val existingDate = existing.date.atTime(existing.startTime.toLocalTime()).atZone(EuropeLondon)
            request to if (existingDate <= ZonedDateTime.now()) {
                val schedule = request.schedule()
                val outcome = schedule.rescheduledBy?.outcomeCode?.let { outcomes[it].orNotFoundBy("code", it) }
                existing.recreate(schedule, outcome)
            } else {
                existing.amendDateTime(with(request.schedule()) {
                    UpdateAppointment.Schedule(date, startTime, endTime, allowConflicts)
                })
            }
        }
    }

    private fun <T> UpdatePipeline<T>.applyOutcome(outcomeProvider: (T) -> UpdateAppointment.Outcome): UpdatePipeline<T> {
        fun T.outcome() = outcomeProvider.invoke(this)
        val outcomes = outcomeRepository.getAllByCodeIn(mapNotNull { (item, _) -> item.outcome().outcomeCode })
        val enforcementAction = enforcementActionRepository.getByCode(EnforcementAction.REFER_TO_PERSON_MANAGER)
        val enforcementReviewType = typeRepository.getByCode(Type.REVIEW_ENFORCEMENT_STATUS)

        return map { (request, existing) ->
            request to existing.applyOutcome(
                outcome = request.outcome().outcomeCode?.let { code -> outcomes[code].orNotFoundBy("code", code) },
                enforcementAction = enforcementAction,
                enforcementReviewType = enforcementReviewType,
            )
        }
    }

    private fun AppointmentContact.applyOutcome(
        outcome: Outcome?,
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

    private fun <T> UpdatePipeline<T>.reassign(assigneeProvider: (T) -> UpdateAppointment.Assignee): UpdatePipeline<T> {
        fun T.assignee() = assigneeProvider.invoke(this)
        val allStaff = staffRepository.getAllByCodeIn(map { (request, _) -> request.assignee().staffCode })
        val allTeams = teamRepository.getAllByCodeIn(map { (request, _) -> request.assignee().teamCode })
        val allLocations =
            locationRepository.getAllByCodeIn(mapNotNull { (request, _) -> request.assignee().locationCode })

        return map { (request, existing) ->
            request to existing.apply {
                val assignee = request.assignee()
                staff = allStaff[assignee.staffCode].orNotFoundBy("code", assignee.staffCode)
                team = allTeams[assignee.teamCode].orNotFoundBy("code", assignee.teamCode)
                officeLocation = assignee.locationCode?.let { allLocations[it].orNotFoundBy("code", it) }
            }
        }
    }

    private fun <T> UpdatePipeline<T>.appendNotes(notesProvider: (T) -> String?): UpdatePipeline<T> {
        fun T.notes() = notesProvider.invoke(this)

        return map { (request, existing) ->
            request to existing.apply {
                notes = listOfNotNull(notes, request.notes()).joinToString("\n\n")
            }
        }
    }

    private fun <T> UpdatePipeline<T>.flagAs(flagsProvider: (T) -> UpdateAppointment.Flags): UpdatePipeline<T> {
        fun T.flags() = flagsProvider.invoke(this)

        return map { (request, existing) ->
            request to existing.apply {
                sensitive = request.flags().sensitive ?: sensitive
                visorContact = request.flags().visor ?: visorContact
                visorExported = if (visorContact == true) visorExported ?: false else null
            }
        }
    }

    private fun AppointmentContact.checkForSchedulingConflicts(allowConflicts: Boolean) = also {
        require(allowConflicts || !appointmentRepository.schedulingConflictExists(this)) {
            "Appointment with reference $externalReference conflicts with an existing appointment"
        }
    }

    private fun AppointmentContact.audit(interactionCode: BusinessInteractionCode) = audit(interactionCode) { audit ->
        audit["offenderId"] = personId
        audit["contactId"] = id!!
    }
}
