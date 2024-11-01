package uk.gov.justice.digital.hmpps.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentDetail
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.CreatedAppointment
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.Period
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

@Service
class SentenceAppointmentService(
    auditedInteractionService: AuditedInteractionService,
    private val appointmentRepository: AppointmentRepository,
    private val appointmentTypeRepository: AppointmentTypeRepository,
    private val offenderManagerRepository: OffenderManagerRepository,
    private val eventSentenceRepository: EventSentenceRepository,
    private val requirementRepository: RequirementRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
) : AuditableService(auditedInteractionService) {
    fun createAppointment(
        crn: String,
        createAppointment: CreateAppointment
    ): AppointmentDetail {
        return audit(BusinessInteractionCode.ADD_CONTACT) { audit ->
            val om = offenderManagerRepository.getByCrn(crn)
            audit["offenderId"] = om.person.id
            checkForConflicts(om.person.id, createAppointment)
            val createAppointments: ArrayList<CreateAppointment> = arrayListOf()

            createAppointment.let {
                val numberOfAppointments = createAppointment.until?.let {
                    Period.between(createAppointment.start.toLocalDate(), it.toLocalDate()).days
                } ?: createAppointment.numberOfAppointments

                for (i in 0 until numberOfAppointments) {
                    val interval = createAppointment.interval.value * i
                    createAppointments.add(
                        CreateAppointment(
                            createAppointment.type,
                            createAppointment.start.plusDays(interval.toLong()),
                            createAppointment.end?.plusDays(interval.toLong()),
                            createAppointment.interval,
                            createAppointment.numberOfAppointments,
                            createAppointment.eventId,
                            if (i == 0 ) createAppointment.uuid else UUID.randomUUID(), //needs to be a unique value
                            createAppointment.requirementId,
                            createAppointment.licenceConditionId,
                            createAppointment.until
                        )
                    )
                }
            }

            val appointments = createAppointments.map { it.withManager(om) }
            val savedAppointments = appointmentRepository.saveAll(appointments)
            val createdAppointments = savedAppointments.map { CreatedAppointment(it.id) }
            audit["contactId"] = createdAppointments.joinToString { it.id.toString()  }

            return@audit AppointmentDetail(createdAppointments)
        }
    }

    private fun checkForConflicts(
        personId: Long,
        createAppointment: CreateAppointment
    ) {
        if (createAppointment.requirementId != null && createAppointment.licenceConditionId != null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Either licence id or requirement id can be provided, not both"
            )
        }

        createAppointment.end?.let {
            if (it.isBefore(createAppointment.start))
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment end time cannot be before start time"
                )
        }

        createAppointment.until?.let {
            if (it.isBefore(createAppointment.start))
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Until cannot be before start time"
                )
        }

        if (!eventSentenceRepository.existsById(createAppointment.eventId)) {
            throw NotFoundException("Event", "eventId", createAppointment.eventId)
        }

        if (createAppointment.requirementId != null && !requirementRepository.existsById(createAppointment.requirementId)) {
            throw NotFoundException("Requirement", "requirementId", createAppointment.requirementId)
        }

        if (createAppointment.licenceConditionId != null && !licenceConditionRepository.existsById(createAppointment.licenceConditionId)) {
            throw NotFoundException("LicenceCondition", "licenceConditionId", createAppointment.licenceConditionId)
        }

        if (createAppointment.start.isAfter(ZonedDateTime.now()) && appointmentRepository.appointmentClashes(
                personId,
                createAppointment.start.toLocalDate(),
                createAppointment.start,
                createAppointment.start
            )
        ) {
            throw ConflictException("Appointment conflicts with an existing future appointment")
        }

        val licenceOrRequirement = listOfNotNull(createAppointment.licenceConditionId, createAppointment.requirementId)

        if (licenceOrRequirement.size > 1) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Either licence id or requirement id can be provided, not both"
            )
        }
    }

    private fun CreateAppointment.withManager(om: OffenderManager) = Appointment(
        om.person,
        appointmentTypeRepository.getByCode(type.code),
        start.toLocalDate(),
        ZonedDateTime.of(LocalDate.EPOCH, start.toLocalTime(), EuropeLondon),
        om.team,
        om.staff,
        0,
        end?.let { ZonedDateTime.of(LocalDate.EPOCH, end.toLocalTime(), EuropeLondon) },
        om.provider.id,
        urn,
        eventId = eventId,
        rqmntId = requirementId,
        licConditionId = licenceConditionId
    )
}