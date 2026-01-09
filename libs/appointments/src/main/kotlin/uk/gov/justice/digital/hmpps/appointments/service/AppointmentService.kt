package uk.gov.justice.digital.hmpps.appointments.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.appointments.domain.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.appointments.domain.contact.AppointmentRepository
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.EnforcementAction
import uk.gov.justice.digital.hmpps.appointments.model.ApplyOutcomeRequest
import uk.gov.justice.digital.hmpps.appointments.model.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentsRepositories
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentsRepositories.getAllByCodeIn
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentsRepositories.getByCode
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

@Service("NewAppointmentService")
@Transactional
class AppointmentService(
    private val teamRepository: AppointmentsRepositories.TeamRepository,
    private val staffRepository: AppointmentsRepositories.StaffRepository,
    private val locationRepository: AppointmentsRepositories.LocationRepository,
    private val contactTypeRepository: AppointmentsRepositories.TypeRepository,
    private val appointmentRepository: AppointmentRepository,
    private val enforcementActionRepository: AppointmentsRepositories.EnforcementActionRepository,
    private val enforcementRepository: AppointmentsRepositories.EnforcementRepository,
    private val outcomeRepository: AppointmentsRepositories.OutcomeRepository,
    private val personRepository: AppointmentsRepositories.PersonRepository,
    auditedInteractionService: AuditedInteractionService,
) : AuditableService(auditedInteractionService) {
    fun scheduleFutureAppointment(request: CreateAppointmentRequest): AppointmentContact {
        require(
            request.date > LocalDate.now() ||
                (request.date == LocalDate.now() && request.startTime > LocalTime.now())
        ) { "Date and time must be in the future to schedule an appointment" }
        // TODO checkForConflicts(request.relatedTo.person.id, request.externalReference, request.schedule)
        return create(request)
    }

    fun create(request: CreateAppointmentRequest): AppointmentContact = create(listOf(request)).single()

    fun create(requests: List<CreateAppointmentRequest>): List<AppointmentContact> =
        appointmentRepository
            .saveAll(requests.asNewEntities())
            .onEach { it.audit(BusinessInteractionCode.ADD_CONTACT) }

    fun AppointmentContact.audit(interactionCode: BusinessInteractionCode) = audit(interactionCode) { audit ->
        audit["offenderId"] = personId
        audit["contactId"] = id!!
    }

    fun applyOutcome(requests: List<ApplyOutcomeRequest>): List<AppointmentContact> =
        requests.asSequence().map { it.reference }.toSet()
            .chunked(500)
            .flatMap { chunk -> appointmentRepository.findByExternalReferenceIn(chunk) }
            .mapNotNull { contact ->
                requests.find { it.reference == contact.externalReference }?.let { it to contact }
            }
            .applyOutcomes()
            .onEach { it.audit(BusinessInteractionCode.UPDATE_CONTACT) }
//        audit(BusinessInteractionCode.UPDATE_CONTACT, username = request.username) { audit ->
//            val outcome = outcomeProvider(request.outcome.code)
//            val appointment = appointmentRepository.getAppointment(request.externalReference).with(outcome)
//            audit["contact_id"] = appointment.id!!
//            appointment
//        }

//    fun update(requests: List<UpdateAppointmentRequest>) {
//        request.appointments.map { "${Contact.REFERENCE_PREFIX}${it.reference}" }.toSet()
//            .chunked(500)
//            .flatMap { chunk -> contactRepository.findByExternalReferenceIn(chunk) }
//            .mapNotNull { contact -> request.findByReference(contact.externalReference!!)?.let { it to contact } }
//            .applyUpdates()
//    }

    //
//    fun delete(request: DeleteAppointmentsRequest) {
//        request.appointments.map { "${Contact.REFERENCE_PREFIX}${it.reference}" }.toSet()
//            .chunked(500)
//            .forEach { contactRepository.softDeleteByExternalReferenceIn(it.toSet()) }
//    }
//
    private fun List<CreateAppointmentRequest>.asNewEntities(): List<AppointmentContact> {
        val types = contactTypeRepository.getAllByCodeIn(map { it.typeCode })
        val outcomes = outcomeRepository.getAllByCodeIn(mapNotNull { it.outcomeCode })
        val enforcementAction = enforcementActionRepository.getByCode(EnforcementAction.REFER_TO_PERSON_MANAGER)
        val locations = locationRepository.getAllByCodeIn(mapNotNull { it.locationCode })
        val teams = teamRepository.getAllByCodeIn(map { it.teamCode })
        val staff = staffRepository.getAllByCodeIn(map { it.staffCode })

        return map { request ->
            val team = teams[request.teamCode].orNotFoundBy("code", request.teamCode)
            val staffMember = staff[request.staffCode].orNotFoundBy("code", request.staffCode)

            AppointmentContact(
                externalReference = request.reference,
                personId = request.relatedTo.personId
                    ?: personRepository.getPerson(requireNotNull(request.relatedTo.crn) { "Either personId or crn must be provided" }).id,
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
            ).apply {
                outcome = request.outcomeCode?.let { code -> outcomes[code].orNotFoundBy("code", code) }
                    ?.also { outcome ->
                        attended = outcome.attended
                        complied = outcome.complied
                        // TODO
//                        if (outcome.complied == false) {
//                            applyEnforcementAction(enforcementAction)
//                            updateFailureToComplyCount()
//                        }
                    }
            }
        }
    }

    private fun List<Pair<ApplyOutcomeRequest, AppointmentContact>>.applyOutcomes(): List<AppointmentContact> {
        val requests = map { (request, _) -> request }
        val outcomes = outcomeRepository.getAllByCodeIn(requests.map { it.outcome.code })
        val enforcementAction = enforcementActionRepository.getByCode(EnforcementAction.REFER_TO_PERSON_MANAGER)

        return map { (request, contact) ->
            contact.apply {
                outcome =
                    request.outcome.code.let { code -> outcomes[code].orNotFoundBy("code", code) }.also { outcome ->
                        complied = outcome.complied
                        attended = outcome.attended
//                        // TODO if (outcome.complied == false) {
//                            applyEnforcementAction(enforcementAction)
//                            updateFailureToComplyCount()
//                        }
                    }
            }
        }
    }
//
//    private fun Contact.applyEnforcementAction(action: EnforcementAction) {
//        if (!enforcementRepository.existsByContactId(id)) {
//            enforcementRepository.save(
//                Enforcement(
//                    contact = this,
//                    action = action,
//                    responseDate = action.responseByPeriod?.let { ZonedDateTime.now().plusDays(it) }
//                )
//            )
//            contactRepository.save(
//                Contact(
//                    linkedContactId = id,
//                    type = action.contactType,
//                    date = LocalDate.now(),
//                    startTime = ZonedDateTime.now(),
//                    person = person,
//                    event = event,
//                    requirement = requirement,
//                    licenceCondition = licenceCondition,
//                    provider = provider,
//                    team = team,
//                    staff = staff,
//                    location = location,
//                    notes = """
//                        |$notes
//                        |
//                        |${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}
//                        |Enforcement Action: ${action.description}
//                    """.trimMargin(),
//                )
//            )
//        }
//        enforcementActionId = action.id
//        enforcement = true
//    }
//
//    private fun Contact.updateFailureToComplyCount() {
//        if (event == null) return
//        event.ftcCount = contactRepository.countFailureToComply(event)
//
//        val ftcLimit = event.disposal?.type?.ftcLimit ?: return
//        if (event.ftcCount > ftcLimit && !contactRepository.enforcementReviewExists(event.id, event.breachEnd)) {
//            createEnforcementReviewContact()
//        }
//    }
//
//    private fun Contact.createEnforcementReviewContact() {
//        contactRepository.save(
//            Contact(
//                linkedContactId = id,
//                type = contactTypeRepository.getByCode(ContactType.REVIEW_ENFORCEMENT_STATUS),
//                date = LocalDate.now(),
//                startTime = ZonedDateTime.now(),
//                person = person,
//                event = event,
//                requirement = requirement,
//                licenceCondition = licenceCondition,
//                provider = provider,
//                team = team,
//                staff = staff,
//                location = location,
//            )
//        )
//    }
//
//    private fun Contact.asAppointment() = AppointmentResponse(
//        crn = person.crn,
//        reference = externalReference?.takeLast(36),
//        requirementId = requirement?.id,
//        licenceConditionId = licenceCondition?.id,
//        date = date,
//        startTime = startTime,
//        endTime = endTime,
//        outcome = outcome?.let { AppointmentOutcome(it.code, it.description, it.attended, it.complied) },
//        location = location?.let { CodedValue(it.code, it.description) },
//        staff = staff.toProbationPractitioner { null },
//        team = team.toCodedValue(),
//        notes = notes,
//        sensitive = sensitive
//    )
//
//    private fun SentenceComponent.commenceComponent(
//        event: Event,
//        request: CreateAppointmentRequest,
//        team: Team,
//        staff: Staff,
//        contactType: ContactType
//    ): Contact? {
//        val shouldCreateCommencedContact = this.commencementDate == null
//        this.commencementDate = request.date.atStartOfDay(EuropeLondon)
//        this.notes = listOfNotNull(
//            this.notes,
//            "Actual Start Date set to ${request.date.toDeliusDate()} following notification from the Accredited Programmes – Intervention Service"
//        ).joinToString(System.lineSeparator() + System.lineSeparator())
//
//        return if (shouldCreateCommencedContact) {
//            Contact(
//                person = event.person.asPersonCrn(),
//                event = event,
//                requirement = this as? Requirement,
//                licenceCondition = this as? LicenceCondition,
//                date = request.date,
//                provider = team.provider,
//                team = team,
//                staff = staff,
//                type = contactType,
//            )
//        } else null
//    }
}

