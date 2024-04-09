package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.activity.Activity
import uk.gov.justice.digital.hmpps.api.model.personalDetails.Document
import uk.gov.justice.digital.hmpps.api.model.schedule.OfficeAddress
import uk.gov.justice.digital.hmpps.api.model.schedule.PersonAppointment
import uk.gov.justice.digital.hmpps.api.model.schedule.Schedule
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.*
import uk.gov.justice.digital.hmpps.integrations.delius.personalDetails.entity.ContactDocument

@Service
class ScheduleService(
    private val personRepository: PersonRepository,
    private val contactRepository: ContactRepository
) {

    @Transactional
    fun getPersonAppointment(crn: String, contactId: Long): PersonAppointment {
        val summary = personRepository.getSummary(crn)
        val contact = contactRepository.getContact(summary.id, contactId)
        return PersonAppointment(
            personSummary = summary.toPersonSummary(),
            appointment = contact.toActivity()
        )
    }

    @Transactional
    fun getPersonUpcomingSchedule(crn: String): Schedule {
        val summary = personRepository.getSummary(crn)
        val appointments = contactRepository.getUpComingAppointments(summary.id)
        return Schedule(
            personSummary = summary.toPersonSummary(),
            appointments = appointments.map { it.toActivity() })
    }

    @Transactional
    fun getPersonPreviousSchedule(crn: String): Schedule {
        val summary = personRepository.getSummary(crn)
        val appointments = contactRepository.getPreviousAppointments(summary.id)
        return Schedule(
            personSummary = summary.toPersonSummary(),
            appointments = appointments.map { it.toActivity() })
    }
}

fun OfficeLocation.toOfficeAddress() = OfficeAddress.from(
    officeName = description,
    buildingName = buildingName,
    buildingNumber = buildingNumber,
    streetName = streetName,
    district = district,
    town = townCity,
    county = county,
    ldu = ldu.description,
    postcode = postcode,
    telephoneNumber = telephoneNumber
)

fun Contact.toActivity() = Activity(
    id = id,
    type = type.description,
    isNationalStandard = type.nationalStandardsContact,
    isSensitive = sensitive,
    didTheyComply = complied,
    acceptableAbsence = outcome?.outcomeAttendance == false && outcome.outcomeCompliantAcceptable == true,
    acceptableAbsenceReason = if (outcome?.outcomeAttendance == false && outcome.outcomeCompliantAcceptable == true)
        outcome.description else null,
    absentWaitingEvidence = attended == false && outcome == null,
    documents = documents.map { it.toDocument() },
    startDateTime = startDateTime(),
    endDateTime = endDateTime(),
    hasOutcome = outcome != null,
    isInitial = isInitial(),
    lastUpdated = lastUpdated,
    lastUpdatedBy = Name(forename = lastUpdatedUser.forename, surname = lastUpdatedUser.surname),
    wasAbsent = outcome?.outcomeAttendance,
    nonComplianceReason = if (outcome?.outcomeCompliantAcceptable == false) type.description else null,
    notes = notes,
    location = location?.toOfficeAddress(),
    officerName = staff?.forename?.let { Name(forename = it, surname = staff.surname) },
    rarCategory = requirement?.mainCategory?.description,
    rarToolKit = requirement?.mainCategory?.description,
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
    isSystemContact = false, //ToDo
    isEmailOrTextFromPop = isEmailOrTextFromPop(),
    isEmailOrTextToPop = isEmailOrTextToPop(),
    isPhoneCallFromPop = isPhoneCallFromPop(),
    isPhoneCallToPop = isPhoneCallToPop(),
    eventNumber = event?.eventNumber
)

fun ContactDocument.toDocument() = Document(id = alfrescoId, name = name, lastUpdated = lastUpdated)
