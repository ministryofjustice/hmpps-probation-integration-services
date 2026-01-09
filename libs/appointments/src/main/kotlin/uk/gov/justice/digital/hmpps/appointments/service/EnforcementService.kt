//package uk.gov.justice.digital.hmpps.appointments.service
//
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import uk.gov.justice.digital.hmpps.appointments.domain.contact.AppointmentRepository
//import uk.gov.justice.digital.hmpps.appointments.domain.contact.AppointmentsLibrary
//import uk.gov.justice.digital.hmpps.appointments.entity.*
//import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentsRepositories
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.time.ZonedDateTime.now
//
//@Transactional
//@Service
//class EnforcementService(
//    private val enforcementActionRepository: AppointmentsRepositories.EnforcementActionRepository,
//    private val enforcementRepository: AppointmentsRepositories.EnforcementRepository,
//    private val appointmentRepository: AppointmentRepository,
//    private val typeRepository: AppointmentsRepositories.TypeRepository,
//) {
//    fun enforceIfNonCompliant(appointment: AppointmentContact) {
//        if (appointment.outcome?.acceptable != false || appointment.outcome?.enforceable != true) {
//            return
//        }
//
//        val action =
//            enforcementActionRepository.getByCode(AppointmentEnforcementAction.Code.REFER_TO_PERSON_MANAGER.value)
//        enforcementRepository.findByContactId(appointment.id!!) ?: AppointmentEnforcement(
//            appointment,
//            action,
//            action.responseByPeriod?.let { now().plusDays(it) }
//        ).apply {
//            enforcementRepository.save(this)
//            val eac = appointment.createEnforcementActionContact(action)
//            appointmentRepository.save(eac)
//        }
//        appointment.enforcementActionId = action.id
//        appointment.enforcement = true
//
//        appointment.event?.let { event ->
//            val currentCount = appointmentRepository.countFailureToComply(
//                event.id,
//                listOfNotNull(event.breachEnd, event.disposal?.date).maxOrNull()
//            )
//            event.ftcCount = currentCount
//            if (event.disposal?.type?.overLimit(currentCount) == true && !event.enforcementUnderReview()) {
//                appointmentRepository.save(appointment.reviewEnforcement())
//            }
//        }
//    }
//    fun AppointmentEntities.DisposalType.overLimit(count: Long): Boolean = sentenceType != null && ftcLimit != null && count > ftcLimit
//
//    private fun AppointmentContact.createEnforcementActionContact(action: AppointmentEnforcementAction) =
//        AppointmentContact(
//            person = person,
//            event = event,
//            requirement = requirement,
//            type = action.type,
//            date = LocalDate.now(),
//            startTime = now(),
//            endTime = null,
//            notes = notes,
//            provider = provider,
//            team = team,
//            staff = staff,
//            location = location,
//            sensitive = null,
//            rarActivity = null,
//            outcome = null,
//            externalReference = null,
//            linkedContactId = this.id
//        ).apply {
//            exportToVisor(null)
//            appendNotes("${LocalDateTime.now()}" + System.lineSeparator() + "Enforcement Action: ${action.description}")
//        }
//
//    private fun AppointmentEntities.Event.enforcementUnderReview() = appointmentRepository.countEnforcementUnderReview(
//        id,
//        AppointmentType.Code.REVIEW_ENFORCEMENT_STATUS.value,
//        breachEnd
//    ) > 0
//
//    private fun AppointmentContact.reviewEnforcement() = AppointmentContact(
//        linkedContactId = this.id,
//        person = person,
//        event = event,
//        requirement = requirement,
//        type = contactTypeRepository.getByCode(AppointmentType.Code.REVIEW_ENFORCEMENT_STATUS.value),
//        date = date,
//        startTime = startTime,
//        endTime = endTime,
//        team = team,
//        staff = staff,
//        provider = provider,
//        location = location,
//    )
//}