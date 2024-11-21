package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.appointment.AppointmentDetail
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.CreatedAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.OverlappingAppointment
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@Service
class SentenceAppointmentService(
    auditedInteractionService: AuditedInteractionService,
    private val appointmentRepository: AppointmentRepository,
    private val appointmentTypeRepository: AppointmentTypeRepository,
    private val offenderManagerRepository: OffenderManagerRepository,
    private val eventSentenceRepository: EventSentenceRepository,
    private val requirementRepository: RequirementRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val staffUserRepository: StaffUserRepository,
    private val objectMapper: ObjectMapper
) : AuditableService(auditedInteractionService) {
    fun createAppointment(
        crn: String,
        createAppointment: CreateAppointment
    ): AppointmentDetail {
        return audit(BusinessInteractionCode.ADD_CONTACT) { audit ->
            val om = offenderManagerRepository.getByCrn(crn)
            audit["offenderId"] = om.person.id

            checkForConflicts(createAppointment)

            val userAndLocation =
                staffUserRepository.getUserAndLocation(createAppointment.user.username, createAppointment.user.team)
            val createAppointments: ArrayList<CreateAppointment> = arrayListOf()

            createAppointment.let {
                val numberOfAppointments = createAppointment.until?.let {
                    val duration = Duration.between(
                        createAppointment.start.toLocalDateTime(),
                        it.toLocalDateTime()
                    ).toDays()

                    (duration / createAppointment.interval.value).toInt() + 1
                } ?: createAppointment.numberOfAppointments

                for (i in 0 until numberOfAppointments) {
                    val interval = createAppointment.interval.value * i
                    createAppointments.add(
                        CreateAppointment(
                            createAppointment.user,
                            createAppointment.type,
                            createAppointment.start.plusDays(interval.toLong()),
                            createAppointment.end.plusDays(interval.toLong()),
                            createAppointment.interval,
                            createAppointment.numberOfAppointments,
                            createAppointment.eventId,
                            if (i == 0) createAppointment.uuid else UUID.randomUUID(), //needs to be a unique value
                            createAppointment.createOverlappingAppointment,
                            createAppointment.requirementId,
                            createAppointment.licenceConditionId,
                            createAppointment.until
                        )
                    )
                }
            }

            val overlappingAppointments = createAppointments.mapNotNull {
                if (it.start.isAfter(ZonedDateTime.now()) && appointmentRepository.appointmentClashes(
                        om.person.id,
                        it.start.toLocalDate(),
                        it.start,
                        it.end
                    )
                ) {
                    OverlappingAppointment(
                        it.start.toLocalDateTime().format(DeliusDateTimeFormatter).dropLast(3),
                        it.end.toLocalDateTime().format(DeliusDateTimeFormatter).dropLast(3)
                    )
                } else null
            }

            if (!createAppointment.createOverlappingAppointment && overlappingAppointments.isNotEmpty()) {
                throw ConflictException(
                    "Appointment(s) conflicts with an existing future appointment ${
                        objectMapper.writeValueAsString(
                            overlappingAppointments
                        )
                    }"
                )
            }

            val appointments = createAppointments.map { it.withManager(om, userAndLocation) }
            val savedAppointments = appointmentRepository.saveAll(appointments)
            val createdAppointments = savedAppointments.map { CreatedAppointment(it.id) }
            audit["contactId"] = createdAppointments.joinToString { it.id.toString() }

            return@audit AppointmentDetail(createdAppointments)
        }
    }

    private fun checkForConflicts(
        createAppointment: CreateAppointment
    ) {
        if (createAppointment.requirementId != null && createAppointment.licenceConditionId != null) {
            throw InvalidRequestException("Either licence id or requirement id can be provided, not both")
        }

        createAppointment.end.let {
            if (it.isBefore(createAppointment.start))
                throw InvalidRequestException("Appointment end time cannot be before start time")
        }

        createAppointment.until?.let {
            if (it.isBefore(createAppointment.start))
                throw InvalidRequestException("Until cannot be before start time")
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

        val licenceOrRequirement = listOfNotNull(createAppointment.licenceConditionId, createAppointment.requirementId)

        if (licenceOrRequirement.size > 1) {
            throw InvalidRequestException("Either licence id or requirement id can be provided, not both")
        }
    }

    private fun CreateAppointment.withManager(om: OffenderManager, userAndLocation: UserLocation) = Appointment(
        om.person,
        appointmentTypeRepository.getByCode(type.code),
        start.toLocalDate(),
        ZonedDateTime.of(LocalDate.EPOCH, start.toLocalTime(), EuropeLondon),
        teamId = userAndLocation.teamId,
        staffId = userAndLocation.staffId,
        0,
        end.let { ZonedDateTime.of(LocalDate.EPOCH, end.toLocalTime(), EuropeLondon) },
        probationAreaId = userAndLocation.providerId,
        urn,
        eventId = eventId,
        rqmntId = requirementId,
        licConditionId = licenceConditionId,
        createdByUserId = userAndLocation.userId,
        officeLocationId = userAndLocation.locationId
    )
}