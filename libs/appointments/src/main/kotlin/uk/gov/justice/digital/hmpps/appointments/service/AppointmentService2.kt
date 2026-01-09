//package uk.gov.justice.digital.hmpps.appointments.service
//
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import uk.gov.justice.digital.hmpps.appointments.domain.audit.BusinessInteractionCode
//import uk.gov.justice.digital.hmpps.appointments.domain.contact.AppointmentRepository
//import uk.gov.justice.digital.hmpps.appointments.domain.contact.appointmentClashes
//import uk.gov.justice.digital.hmpps.appointments.domain.contact.getAppointment
//import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentContact
//import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentOutcome
//import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentType
//import uk.gov.justice.digital.hmpps.appointments.model.*
//import uk.gov.justice.digital.hmpps.audit.service.AuditableService
//import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
//import uk.gov.justice.digital.hmpps.exception.ConflictException
//
//@Transactional
//@Service("_AppointmentService")
//class AppointmentService(
//    auditedInteractionService: AuditedInteractionService,
//    private val relatedToService: RelatedToService,
//    private val appointmentRepository: AppointmentRepository,
//    private val assignationService: AssignationService,
//    private val enforcementService: EnforcementService,
//) : AuditableService(auditedInteractionService) {
//    fun scheduleAppointment(
//        request: CreateFutureAppointment,
//        typeProvider: (String) -> AppointmentType
//    ): AppointmentContact =
//        audit(BusinessInteractionCode.ADD_CONTACT, username = request.username) { audit ->
//            require(request.schedule.isInTheFuture()) {
//                "Date and time must be in the future to schedule an appointment"
//            }
//            checkForConflicts(request.relatedTo.person.id, request.externalReference, request.schedule)
//            val relatedTo = relatedToService.findRelatedTo(request.relatedTo)
//            audit["offender_id"] = relatedTo.person.id
//            val type = typeProvider(request.type.code)
//            val assignation = assignationService.retrieveAssignation(request.assigned, relatedTo.person.manager)
//            val appointment = appointmentRepository.save(request.asEntity(relatedTo, type, assignation))
//            audit["contact_id"] = appointment.id!!
//            appointment
//        }
//
//    fun applyOutcome(
//        request: ApplyOutcomeRequest,
//        outcomeProvider: (String) -> AppointmentOutcome
//    ): AppointmentContact =
//        audit(BusinessInteractionCode.UPDATE_CONTACT, username = request.username) { audit ->
//            val outcome = outcomeProvider(request.outcome.code)
//            val appointment = appointmentRepository.getAppointment(request.externalReference).with(outcome)
//            audit["contact_id"] = appointment.id!!
//            appointment
//        }
//
//    fun recordAppointment(
//        request: RecordAppointmentRequest,
//        typeProvider: (String) -> AppointmentType,
//        outcomeProvider: (String) -> AppointmentOutcome
//    ): AppointmentContact = audit(BusinessInteractionCode.ADD_CONTACT, username = request.username) { audit ->
//        val relatedTo = relatedToService.findRelatedTo(request.relatedTo)
//        audit["offender_id"] = relatedTo.person.id
//        val type = typeProvider(request.type.code)
//        val assignation = assignationService.retrieveAssignation(request.assigned, relatedTo.person.manager)
//        val outcome = outcomeProvider(request.outcome.code)
//        val appointment = appointmentRepository.save(request.asEntity(relatedTo, type, assignation)).with(outcome)
//        audit["contact_id"] = appointment.id!!
//        appointment
//    }
//
//    private fun checkForConflicts(
//        crn: String,
//        externalReference: String,
//        schedule: Schedule
//    ) {
//        if (appointmentRepository.appointmentClashes(
//                crn,
//                externalReference,
//                schedule.start.toLocalDate(),
//                schedule.start,
//                schedule.end
//            )
//        ) {
//            throw ConflictException("Appointment conflicts with an existing future appointment")
//        }
//    }
//
//    private fun AppointmentContact.with(outcome: AppointmentOutcome) = apply {
//        applyOutcome(outcome)
//        enforcementService.enforceIfNonCompliant(this)
//    }
//}
//
//private fun AppointmentRequest.asEntity(
//    relatedTo: RelatedTo,
//    type: AppointmentType,
//    assign: Assignation,
//) = AppointmentContact(
//    person = relatedTo.person,
//    event = relatedTo.event,
//    requirement = relatedTo.requirement,
//    type = type,
//    staff = assign.staff,
//    team = assign.team,
//    location = assign.location,
//    date = schedule.start.toLocalDate(),
//    startTime = schedule.start,
//    endTime = schedule.end,
//    provider = assign.team.provider,
//    outcome = null,
//    rarActivity = type.rarActivity,
//    notes = notes,
//    sensitive = flagAs.sensitive || type.sensitive,
//    externalReference = externalReference,
//    softDeleted = false
//).apply { exportToVisor(flagAs.visor) }
//
