package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.appointment.*
import uk.gov.justice.digital.hmpps.aspect.UserContext
import uk.gov.justice.digital.hmpps.audit.service.AuditableService
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.BankHolidayClient
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import uk.gov.justice.digital.hmpps.utils.AppointmentTimeHelper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime

@Service
class SentenceAppointmentService(
    auditedInteractionService: AuditedInteractionService,
    private val sentenceAppointmentRepository: SentenceAppointmentRepository,
    private val appointmentTypeRepository: AppointmentTypeRepository,
    private val offenderManagerRepository: OffenderManagerRepository,
    private val eventSentenceRepository: EventSentenceRepository,
    private val requirementRepository: RequirementRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val staffUserRepository: StaffUserRepository,
    private val locationRepository: LocationRepository,
    private val nsiRepository: NsiRepository,
    private val bankHolidayClient: BankHolidayClient,
    private val userService: UserService
) : AuditableService(auditedInteractionService) {

    private fun getOverlaps(
        personId: Long,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Pair<List<StaffAppointment>, List<StaffAppointment>> {
        val withinHourOf = 1L
        val overlaps = sentenceAppointmentRepository.staffAppointmentClashes(
            personId,
            start.toLocalDate(),
            start.minusHours(withinHourOf),
            end.plusHours(withinHourOf)
        )
        return overlaps.partition {
            it.startDateTime.atZone(EuropeLondon).isBefore(end) &&
                it.endDateTime.atZone(EuropeLondon)?.isAfter(start) == true
        }
    }

    private fun nonWorkingDay(date: LocalDate): String? {
        if (listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.dayOfWeek)) {
            return date.dayOfWeek.name.lowercase().replaceFirstChar(Char::titlecase)
        }
        return try {
            bankHolidayClient.getBankHolidays().englandAndWales.events.firstOrNull { it.date == date }?.title
        } catch (ex: Exception) {
            null
        }
    }

    fun StaffAppointment.toAppointmentCheck() = AppointmentCheck(
        appointmentIsWith = Name(forename = forename, surname = surname),
        isCurrentUser = userService.getUserStaffId() == staffId,
        startAndEnd = AppointmentTimeHelper.startAndEnd(
            startDateTime.atZone(EuropeLondon),
            endDateTime.atZone(EuropeLondon)
        ),
    )

    fun checkAppointment(crn: String, checkAppointment: CheckAppointment): AppointmentChecks {
        val om = offenderManagerRepository.getByCrn(crn)
        val overlaps = getOverlaps(om.person.id, checkAppointment.start, checkAppointment.end)
        return AppointmentChecks(
            nonWorkingDayName = nonWorkingDay(checkAppointment.start.toLocalDate()),
            overlapsWithMeetingWith = overlaps.first.firstOrNull()?.toAppointmentCheck(),
            isWithinOneHourOfMeetingWith = overlaps.second.firstOrNull()?.toAppointmentCheck(),
        )
    }

    @Transactional
    fun createAppointment(
        crn: String,
        createAppointment: CreateAppointment
    ): AppointmentDetail {
        return audit(BusinessInteractionCode.ADD_CONTACT, username = UserContext.get()?.username) { audit ->
            val om = offenderManagerRepository.getByCrn(crn)
            audit["offenderId"] = om.person.id

            checkForConflicts(createAppointment)

            val userAndTeam = staffUserRepository.getUserAndTeamAssociation(
                createAppointment.user.username,
                createAppointment.user.teamCode
            )

            val location = createAppointment.user.locationCode?.let {
                locationRepository.getTeamAndLocation(
                    createAppointment.user.teamCode,
                    createAppointment.user.locationCode
                )
            }

            val saved = sentenceAppointmentRepository.save(createAppointment.withManager(om, userAndTeam, location))

            audit["contactId"] = saved.id

            return@audit AppointmentDetail(listOf(CreatedAppointment(saved.id, saved.externalReference)))
        }
    }

    private fun checkForConflicts(
        createAppointment: CreateAppointment
    ) {
        val appointmentIds = listOfNotNull(
            createAppointment.requirementId,
            createAppointment.licenceConditionId,
            createAppointment.nsiId
        )
        if (appointmentIds.size > 1) {
            throw InvalidRequestException("Either licence id or requirement id or nsi id can be provided")
        }

        if (createAppointment.eventId == null && (createAppointment.requirementId != null || createAppointment.licenceConditionId != null)) {
            throw InvalidRequestException("Event id required when licence id or requirement id provided")
        }

        createAppointment.end.let {
            if (it.isBefore(createAppointment.start))
                throw InvalidRequestException("Appointment end time cannot be before start time")
        }

        createAppointment.until?.let {
            if (it.isBefore(createAppointment.start))
                throw InvalidRequestException("Until cannot be before start time")
        }

        createAppointment.eventId?.let {
            if (!eventSentenceRepository.existsById(it)) {
                throw NotFoundException("Event", "eventId", createAppointment.eventId)
            }
        }

        if (createAppointment.requirementId != null && !requirementRepository.existsById(createAppointment.requirementId)) {
            throw NotFoundException("Requirement", "requirementId", createAppointment.requirementId)
        }

        if (createAppointment.licenceConditionId != null && !licenceConditionRepository.existsById(createAppointment.licenceConditionId)) {
            throw NotFoundException("LicenceCondition", "licenceConditionId", createAppointment.licenceConditionId)
        }

        if (createAppointment.nsiId != null && !nsiRepository.existsById(createAppointment.nsiId)) {
            throw NotFoundException("Nsi", "nsiId", createAppointment.nsiId)
        }

        val contactType = appointmentTypeRepository.getByCode(createAppointment.type)

        if (contactType.locationRequired == "Y" && createAppointment.user.locationCode == null) {
            throw InvalidRequestException("Location required for contact type ${createAppointment.type}")
        }

        if (!contactType.offenderContact && (listOfNotNull(createAppointment.eventId) + appointmentIds).isEmpty()) {
            throw InvalidRequestException("Event id, licence id, requirement id or nsi id need to be provided for contact type ${createAppointment.type}")
        }

        sentenceAppointmentRepository.findByExternalReference(createAppointment.urn)?.let {
            throw ConflictException("Duplicate external reference ${createAppointment.urn}")
        }
    }

    private fun CreateAppointment.withManager(
        om: OffenderManager,
        staffAndTeam: UserTeam,
        location: Location?
    ) = SentenceAppointment(
        person = om.person,
        type = appointmentTypeRepository.getByCode(type),
        date = start.toLocalDate(),
        startTime = ZonedDateTime.of(LocalDate.EPOCH, start.toLocalTime(), EuropeLondon),
        teamId = staffAndTeam.teamId,
        staffId = staffAndTeam.staffId,
        endTime = end.let { ZonedDateTime.of(LocalDate.EPOCH, end.toLocalTime(), EuropeLondon) },
        probationAreaId = staffAndTeam.providerId,
        externalReference = urn,
        eventId = eventId,
        rqmntId = requirementId,
        nsiId = nsiId,
        licConditionId = licenceConditionId,
        officeLocationId = location?.id,
        notes = notes,
        sensitive = sensitive,
        visorContact = visorReport
    )
}
