package uk.gov.justice.digital.hmpps.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.activity.Activity
import uk.gov.justice.digital.hmpps.api.model.activity.Component
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Document
import uk.gov.justice.digital.hmpps.api.model.schedule.*
import uk.gov.justice.digital.hmpps.api.model.user.PersonManager
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactDocument
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCrn
import java.time.ZonedDateTime

@Transactional
@Service
class ScheduleService(
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository,
    private val comRepository: OffenderManagerRepository,
) {

    fun getPersonAppointment(crn: String, contactId: Long, noteId: Int? = null): PersonAppointment {
        val summary = personRepository.getSummary(crn)
        val contact = contactRepository.getContact(summary.id, contactId)
        return PersonAppointment(
            personSummary = summary.toPersonSummary(),
            appointment = contact.toActivity(noteId)
        )
    }

    fun getPersonUpcomingSchedule(crn: String, pageable: Pageable): Schedule {
        val summary = personRepository.getSummary(crn)
        val appointments = contactRepository.getUpComingAppointments(summary.id, pageable)
        return Schedule(
            personSummary = summary.toPersonSummary(),
            personSchedule = PersonSchedule(
                pageable.pageSize,
                pageable.pageNumber,
                appointments.totalElements.toInt(),
                appointments.totalPages,
                appointments.content.map { it.toActivity() }

            )
        )
    }

    fun getPersonPreviousSchedule(crn: String, pageable: Pageable): Schedule {
        val summary = personRepository.getSummary(crn)
        val appointments = contactRepository.getPageablePreviousAppointments(summary.id, pageable)
        return Schedule(
            personSummary = summary.toPersonSummary(),
            personSchedule = PersonSchedule(
                pageable.pageSize,
                pageable.pageNumber,
                appointments.totalElements.toInt(),
                appointments.totalPages,
                appointments.content.map { it.toActivity() }

            )
        )
    }

    fun getNextComAppointment(crn: String, contactId: Long, username: String): NextAppointment? {
        val com = comRepository.getByCrn(crn)
        val initialAppointment = contactRepository.getContact(com.person.id, contactId)
        val initialDateTime = ZonedDateTime.of(
            initialAppointment.date,
            initialAppointment.startTime?.toLocalTime() ?: initialAppointment.date.atStartOfDay().toLocalTime(),
            EuropeLondon
        )
        val dateTime = maxOf(ZonedDateTime.now(EuropeLondon), initialDateTime)
        val nextAppointment = contactRepository.firstAppointment(com.person.id, dateTime.toLocalDate(), dateTime)
        return NextAppointment(
            nextAppointment?.toActivity(),
            com.asPersonManager(),
            username.equals(com.staff.user?.username, ignoreCase = true)
        )
    }
}

fun OfficeLocation.toOfficeAddress() = OfficeAddress.from(
    code = code,
    officeName = description,
    buildingName = buildingName,
    buildingNumber = buildingNumber,
    streetName = streetName,
    district = district,
    town = townCity,
    county = county,
    ldu = ldu.description,
    postcode = postcode,
    telephoneNumber = telephoneNumber,
)

fun Contact.toActivityOverview() = Activity(
    id = id,
    type = type.description,
    isNationalStandard = type.nationalStandardsContact,
    isSensitive = sensitive,
    didTheyComply = complied,
    acceptableAbsence = outcome?.outcomeAttendance == false && outcome.outcomeCompliantAcceptable == true,
    acceptableAbsenceReason = if (outcome?.outcomeAttendance == false && outcome.outcomeCompliantAcceptable == true)
        outcome.description else null,
    absentWaitingEvidence = attended == false && outcome == null,
    startDateTime = startDateTime(),
    endDateTime = endDateTime(),
    hasOutcome = hasARequiredOutcome(),
    isInitial = isInitial(),
    lastUpdated = lastUpdated,
    lastUpdatedBy = Name(forename = lastUpdatedUser.forename, surname = lastUpdatedUser.surname),
    wasAbsent = outcome?.outcomeAttendance == false,
    nonComplianceReason = if (outcome?.outcomeCompliantAcceptable == false) type.description else null,
    countsTowardsRAR = rarActivity,
    rescheduled = rescheduledPop(),
    rescheduledStaff = rescheduledPop() || rescheduledStaff(),
    rescheduledPop = rescheduledPop(),
    rearrangeOrCancelReason = if (rescheduled()) outcome?.description else null,
    isAppointment = type.attendanceContact,
    action = action?.description,
    isSystemContact = type.systemGenerated,
    isEmailOrTextFromPop = isEmailOrTextFromPop(),
    isEmailOrTextToPop = isEmailOrTextToPop(),
    isPhoneCallFromPop = isPhoneCallFromPop(),
    isPhoneCallToPop = isPhoneCallToPop(),
    isCommunication = isCommunication(),
    description = description,
    outcome = outcome?.description,
    deliusManaged = CreateAppointment.Type.entries.none { it.code == type.code } || complied == false || requirement?.mainCategory?.code == "F",
)

fun Contact.toActivity(noteId: Int? = null) = Activity(
    id = id,
    type = type.description,
    isNationalStandard = type.nationalStandardsContact,
    isSensitive = sensitive,
    didTheyComply = if (type.attendanceContact) {
        complied
    } else null,
    acceptableAbsence = outcome?.outcomeAttendance == false && outcome.outcomeCompliantAcceptable == true,
    acceptableAbsenceReason = if (outcome?.outcomeAttendance == false && outcome.outcomeCompliantAcceptable == true)
        outcome.description else null,
    absentWaitingEvidence = attended == false && outcome == null,
    documents = documents.map { it.toDocument() },
    startDateTime = startDateTime(),
    endDateTime = endDateTime(),
    hasOutcome = hasARequiredOutcome(),
    isInitial = isInitial(),
    lastUpdated = lastUpdated,
    lastUpdatedBy = Name(forename = lastUpdatedUser.forename, surname = lastUpdatedUser.surname),
    wasAbsent = outcome?.outcomeAttendance == false,
    nonComplianceReason = if (outcome?.outcomeCompliantAcceptable == false) type.description else null,
    appointmentNotes = if (noteId == null) formatNote(notes, true) else null,
    appointmentNote = if (noteId != null) formatNote(notes, false).elementAtOrNull(noteId) else null,
    location = location?.toOfficeAddress(),
    officer = staff?.let {
        Manager(
            it.code,
            Name(forename = it.forename, surname = it.surname),
            team!!.code,
            team.provider.code,
            it.user?.username
        )
    },
    isRarRelated = requirement?.mainCategory?.code == "F",
    rarCategory = requirement?.mainCategory?.description,
    rarToolKit = requirement?.mainCategory?.description,
    countsTowardsRAR = rarActivity,
    rescheduled = rescheduledPop(),
    rescheduledStaff = rescheduledPop() || rescheduledStaff(),
    rescheduledPop = rescheduledPop(),
    rearrangeOrCancelReason = if (rescheduled()) outcome?.description else null,
    rescheduledBy = if (rescheduled()) Name(
        forename = lastUpdatedUser.forename,
        surname = lastUpdatedUser.surname
    ) else null,
    isAppointment = type.attendanceContact,
    action = action?.description,
    isSystemContact = type.systemGenerated,
    isEmailOrTextFromPop = isEmailOrTextFromPop(),
    isEmailOrTextToPop = isEmailOrTextToPop(),
    isPhoneCallFromPop = isPhoneCallFromPop(),
    isPhoneCallToPop = isPhoneCallToPop(),
    isCommunication = isCommunication(),
    eventNumber = event?.eventNumber,
    description = description,
    outcome = outcome?.description,
    deliusManaged = CreateAppointment.Type.entries.none { it.code == type.code } || complied == false || requirement?.mainCategory?.code == "F",
    isVisor = isVisor,
    eventId = event?.id,
    component = requirement?.asComponent() ?: licenceCondition?.asComponent(),
    nsiId = nsiId,
)

fun ContactDocument.toDocument() =
    Document(id = alfrescoId, name = name, createdAt = createdAt, lastUpdated = lastUpdated)

fun OffenderManager.asPersonManager(): PersonManager = with(staff) {
    PersonManager(Name(forename, null, surname))
}

fun Requirement.asComponent() =
    Component(id, mainCategory?.description ?: "", Component.Type.REQUIREMENT)

fun LicenceCondition.asComponent() = Component(id, mainCategory.description, Component.Type.LICENCE_CONDITION)