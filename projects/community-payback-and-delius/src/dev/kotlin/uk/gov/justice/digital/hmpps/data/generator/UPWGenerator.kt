package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactOutcome
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.contact.EnforcementAction
import uk.gov.justice.digital.hmpps.entity.person.Address
import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.sentence.*
import uk.gov.justice.digital.hmpps.entity.staff.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.staff.Provider
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.entity.unpaidwork.*
import uk.gov.justice.digital.hmpps.model.Behaviour
import uk.gov.justice.digital.hmpps.model.WorkQuality
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object UPWGenerator {
    val DEFAULT_ADDRESS = generateAddress(
        addressNumber = "123",
        streetName = "Test Street",
        town = "Town",
        postcode = "AB12CD"
    )
    val DEFAULT_OFFICE_LOCATION = generateOfficeLocation(
        code = "LOC0001",
        addressNumber = "1001",
        streetName = "Office Street",
        town = "City",
        postcode = "ZY98XW"
    )

    val UPW_PROJECT_1 = generateUpwProject(
        name = "Default UPW Project",
        code = "N01P01",
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        placementAddress = DEFAULT_ADDRESS,
        projectType = ReferenceDataGenerator.GROUP_PLACEMENT_PROJECT_TYPE,
        expectedEndDate = LocalDate.now().plusMonths(4)
    )
    val UPW_PROJECT_2 = generateUpwProject(
        name = "Second UPW Project",
        code = "N01P02",
        team = TeamGenerator.OTHER_PROVIDER_TEAM,
        placementAddress = DEFAULT_ADDRESS,
        projectType = ReferenceDataGenerator.INDIVIDUAL_PLACEMENT_PROJECT_TYPE
    )
    val UPW_PROJECT_3 = generateUpwProject(
        name = "Third UPW Project",
        code = "N01P03",
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        placementAddress = DEFAULT_ADDRESS,
        projectType = ReferenceDataGenerator.GROUP_PLACEMENT_PROJECT_TYPE
    )
    val COMPLETED_UPW_PROJECT = generateUpwProject(
        name = "Completed UPW Project",
        code = "N01COMP",
        completionDate = LocalDate.now().minusDays(1),
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        placementAddress = DEFAULT_ADDRESS,
        projectType = ReferenceDataGenerator.GROUP_PLACEMENT_PROJECT_TYPE
    )

    val DEFAULT_UPW_PROJECT_AVAILABILITY = generateUpwProjectAvailability(
        project = UPW_PROJECT_1,
        frequency = ReferenceDataGenerator.UPW_FREQUENCY_WEEKLY,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(16, 0),
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusMonths(4)
    )
    val SECOND_UPW_PROJECT_AVAILABILITY = generateUpwProjectAvailability(
        project = UPW_PROJECT_2,
    )

    val EVENT_1 = generateEvent(
        eventNumber = "1",
        person = PersonGenerator.DEFAULT_PERSON,
        disposal = null
    )

    val EVENT_2 = generateEvent(
        eventNumber = "2",
        person = PersonGenerator.DEFAULT_PERSON,
        disposal = null
    )

    val EVENT_3 = generateEvent(
        eventNumber = "3",
        person = PersonGenerator.DEFAULT_PERSON,
        disposal = null
    )

    val DISPOSAL_1 = generateDisposal(
        length = 12,
        disposalType = ReferenceDataGenerator.DEFAULT_DISPOSAL_TYPE,
        date = LocalDate.of(2026, 1, 1),
        event = EVENT_1
    )
    val DISPOSAL_2 = generateDisposal(
        length = 12,
        disposalType = ReferenceDataGenerator.DEFAULT_DISPOSAL_TYPE,
        date = LocalDate.of(2026, 1, 1),
        event = EVENT_2
    )
    val DISPOSAL_3 = generateDisposal(
        length = 12,
        disposalType = ReferenceDataGenerator.DEFAULT_DISPOSAL_TYPE,
        date = LocalDate.of(2026, 1, 1),
        event = EVENT_3
    )

    val UPW_DETAILS_1 = generateUpwDetails(disposal = DISPOSAL_1)
    val UPW_DETAILS_2 = generateUpwDetails(disposal = DISPOSAL_2)
    val UPW_DETAILS_3 = generateUpwDetails(disposal = DISPOSAL_3)

    val DEFAULT_UPW_ALLOCATION = generateUpwAllocation(
        details = UPW_DETAILS_1,
        project = UPW_PROJECT_1,
        projectAvailability = DEFAULT_UPW_PROJECT_AVAILABILITY,
        allocationDay = ReferenceDataGenerator.UPW_DAY_MONDAY,
        requestedFrequency = ReferenceDataGenerator.UPW_FREQUENCY_WEEKLY,
        startDate = LocalDate.now().minusDays(7),
        endDate = LocalDate.now().plusMonths(3),
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(16, 0)
    )

    val DEFAULT_CONTACT = generateContact(
        contactType = ReferenceDataGenerator.UPW_APPOINTMENT_TYPE,
        latestEnforcementAction = ReferenceDataGenerator.ROM_ENFORCEMENT_ACTION,
        contactOutcome = ReferenceDataGenerator.FAILED_TO_ATTEND_CONTACT_OUTCOME,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        date = LocalDate.now(),
        personId = PersonGenerator.DEFAULT_PERSON.id,
        officeLocation = DEFAULT_OFFICE_LOCATION,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        provider = ProviderGenerator.DEFAULT_PROVIDER
    )
    val CONTACT_NO_ENFORCEMENT = generateContact(
        contactType = ReferenceDataGenerator.UPW_APPOINTMENT_TYPE,
        latestEnforcementAction = null,
        contactOutcome = null,
        startTime = LocalTime.of(10, 15),
        endTime = LocalTime.of(16, 30),
        date = LocalDate.now().minusDays(1),
        personId = PersonGenerator.DEFAULT_PERSON.id,
        officeLocation = DEFAULT_OFFICE_LOCATION,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        provider = ProviderGenerator.DEFAULT_PROVIDER,
        event = EVENT_1
    )

    val DEFAULT_UPW_APPOINTMENT = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        date = LocalDate.now().plusDays(1),
        project = UPW_PROJECT_1,
        details = UPW_DETAILS_1,
        allocation = DEFAULT_UPW_ALLOCATION,
        contact = DEFAULT_CONTACT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = 65L,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.WORK_QUALITY[WorkQuality.EXCELLENT],
        behaviour = ReferenceDataGenerator.BEHAVIOUR[Behaviour.EXCELLENT],
        minutesCredited = 30L
    )

    val LAO_EXCLUDED_UPW_APPOINTMENT = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        date = LocalDate.now(),
        project = UPW_PROJECT_1,
        details = UPW_DETAILS_1,
        contact = DEFAULT_CONTACT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = 60L,
        person = PersonGenerator.EXCLUDED_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.WORK_QUALITY[WorkQuality.EXCELLENT],
        behaviour = ReferenceDataGenerator.BEHAVIOUR[Behaviour.EXCELLENT],
        minutesCredited = 420L
    )

    val LAO_RESTRICTED_UPW_APPOINTMENT = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        date = LocalDate.now(),
        project = UPW_PROJECT_1,
        details = UPW_DETAILS_1,
        contact = DEFAULT_CONTACT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = 60L,
        person = PersonGenerator.RESTRICTED_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.WORK_QUALITY[WorkQuality.EXCELLENT],
        behaviour = ReferenceDataGenerator.BEHAVIOUR[Behaviour.EXCELLENT],
        minutesCredited = 420L
    )

    val UPW_APPOINTMENT_NO_ENFORCEMENT = generateUpwAppointment(
        startTime = LocalTime.of(10, 15),
        endTime = LocalTime.of(16, 30),
        date = LocalDate.now().minusDays(1),
        project = UPW_PROJECT_2,
        details = UPW_DETAILS_2,
        contact = CONTACT_NO_ENFORCEMENT,
        contactOutcomeTypeId = null,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(10, 15),
        penaltyTime = null,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.WORK_QUALITY[WorkQuality.EXCELLENT],
        behaviour = ReferenceDataGenerator.BEHAVIOUR[Behaviour.EXCELLENT],
        minutesCredited = 375L,
    )

    val UPW_APPOINTMENT_TO_UPDATE = List(10) {
        generateUpwAppointment(
            startTime = LocalTime.of(12, 0),
            endTime = LocalTime.of(14, 0),
            date = LocalDate.now().minusDays(1),
            project = UPW_PROJECT_2,
            details = UPW_DETAILS_3,
            contact = generateContact(
                personId = PersonGenerator.DEFAULT_PERSON.id,
                event = EVENT_2,
                contactType = ReferenceDataGenerator.UPW_APPOINTMENT_TYPE,
                latestEnforcementAction = null,
                contactOutcome = null,
                startTime = LocalTime.of(12, 0),
                endTime = LocalTime.of(14, 0),
                date = LocalDate.now().minusDays(1),
                officeLocation = DEFAULT_OFFICE_LOCATION,
                staff = StaffGenerator.DEFAULT_STAFF,
                team = TeamGenerator.DEFAULT_UPW_TEAM,
                provider = ProviderGenerator.DEFAULT_PROVIDER,
            ),
            contactOutcomeTypeId = null,
            pickupLocation = DEFAULT_OFFICE_LOCATION,
            pickupTime = LocalTime.of(12, 0),
            penaltyTime = null,
            person = PersonGenerator.DEFAULT_PERSON,
            staff = StaffGenerator.DEFAULT_STAFF,
            team = TeamGenerator.DEFAULT_UPW_TEAM,
            workQuality = null,
            behaviour = null,
            minutesCredited = null,
        )
    }

    val DEFAULT_RQMNT = generateRequirement(
        length = 120,
        disposal = DISPOSAL_1
    )

    val SECOND_RQMNT = generateRequirement(
        length = 180,
        disposal = DISPOSAL_2
    )

    val DEFAULT_UPW_DETAILS_ADJUSTMENT_POSITIVE = generateUPWAdjustment(
        upwDetailsId = UPW_DETAILS_1.id,
        adjustmentAmount = 7L,
        adjustmentType = "POSITIVE"
    )

    val DEFAULT_UPW_DETAILS_ADJUSTMENT_NEGATIVE = generateUPWAdjustment(
        upwDetailsId = UPW_DETAILS_1.id,
        adjustmentAmount = 3L,
        adjustmentType = "NEGATIVE"
    )

    fun generateUpwProject(
        id: Long = IdGenerator.getAndIncrement(),
        name: String,
        code: String,
        team: Team,
        placementAddress: Address?,
        projectType: ReferenceData,
        availability: List<UpwProjectAvailability> = listOf(),
        hiVisRequired: Boolean = false,
        expectedEndDate: LocalDate? = null,
        completionDate: LocalDate? = null
    ) = UpwProject(
        id,
        name,
        code,
        team,
        placementAddress,
        projectType,
        availability,
        hiVisRequired,
        expectedEndDate,
        completionDate
    )

    fun generateUpwProjectAvailability(
        id: Long = IdGenerator.getAndIncrement(),
        project: UpwProject,
        day: UnpaidWorkDay = ReferenceDataGenerator.UPW_DAY_MONDAY,
        frequency: ReferenceData? = null,
        startTime: LocalTime? = null,
        endTime: LocalTime? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ) = UpwProjectAvailability(id, project, day, frequency, startTime, endTime, startDate, endDate)

    fun generateDisposal(
        id: Long = IdGenerator.getAndIncrement(),
        disposalType: DisposalType,
        date: LocalDate,
        length: Long,
        event: Event,
        softDeleted: Boolean = false,
    ) = Disposal(id, disposalType, date, length, event, softDeleted)

    fun generateUpwDetails(
        id: Long = IdGenerator.getAndIncrement(),
        disposal: Disposal,
        softDeleted: Boolean = false
    ) = UnpaidWorkDetails(id, disposal, softDeleted)

    fun generateUPWAdjustment(
        id: Long = IdGenerator.getAndIncrement(),
        upwDetailsId: Long,
        adjustmentAmount: Long,
        adjustmentType: String,
    ) = UnpaidWorkAdjustment(id, upwDetailsId, adjustmentAmount, adjustmentType)

    fun generateContact(
        id: Long = id(),
        contactType: ContactType,
        contactOutcome: ContactOutcome?,
        attended: Boolean? = true,
        complied: Boolean? = true,
        latestEnforcementAction: EnforcementAction?,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime?,
        personId: Long,
        event: Event? = null,
        requirementId: Long? = null,
        licenceConditionId: Long? = null,
        officeLocation: OfficeLocation,
        staff: Staff,
        team: Team,
        provider: Provider,
        notes: String? = null,
        sensitive: Boolean? = false,
        alertsActive: Boolean? = false,
        rowVersion: Long = 1,
    ) = Contact(
        id = id,
        rowVersion = rowVersion,
        externalReference = null,
        contactType = contactType,
        outcome = contactOutcome,
        attended = attended,
        complied = complied,
        latestEnforcementAction = latestEnforcementAction,
        date = date,
        startTime = startTime,
        endTime = endTime,
        personId = personId,
        event = event,
        requirementId = requirementId,
        licenceConditionId = licenceConditionId,
        officeLocation = officeLocation,
        staff = staff,
        team = team,
        provider = provider,
        notes = notes,
        sensitive = sensitive,
        alertActive = alertsActive
    )

    fun generateUpwAppointment(
        id: Long = id(),
        attended: Boolean? = true,
        complied: Boolean? = true,
        softDeleted: Boolean = false,
        startTime: LocalTime,
        endTime: LocalTime,
        date: LocalDate,
        project: UpwProject,
        details: UnpaidWorkDetails,
        allocation: UnpaidWorkAllocation? = null,
        pickupLocation: OfficeLocation?,
        pickupTime: LocalTime?,
        penaltyTime: Long?,
        contact: Contact,
        contactOutcomeTypeId: Long?,
        person: Person,
        staff: Staff,
        team: Team,
        hiVisWorn: Boolean = false,
        workedIntensively: Boolean = false,
        workQuality: ReferenceData?,
        behaviour: ReferenceData?,
        minutesCredited: Long?,
        notes: String? = null,
        rowVersion: Long = 1,
        createdDatetime: ZonedDateTime = ZonedDateTime.now(),
        lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
    ) = UnpaidWorkAppointment(
        id = id,
        attended = attended,
        complied = complied,
        softDeleted = softDeleted,
        date = date,
        startTime = startTime,
        endTime = endTime,
        project = project,
        details = details,
        allocation = allocation,
        pickUpTime = pickupTime,
        pickUpLocation = pickupLocation,
        penaltyMinutes = penaltyTime,
        contact = contact,
        outcomeId = contactOutcomeTypeId,
        person = person,
        staff = staff,
        team = team,
        hiVisWorn = hiVisWorn,
        workedIntensively = workedIntensively,
        workQuality = workQuality,
        behaviour = behaviour,
        minutesCredited = minutesCredited,
        notes = notes,
        rowVersion = rowVersion,
        createdDatetime = createdDatetime,
        createdByUserId = 0,
        lastUpdatedDatetime = lastUpdatedDatetime,
        lastUpdatedUserId = 0,
    )

    fun generateAddress(
        id: Long = IdGenerator.getAndIncrement(),
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null
    ) = Address(id, buildingName, addressNumber, streetName, town, county, postcode)

    fun generateOfficeLocation(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null
    ) = OfficeLocation(id, code, buildingName, addressNumber, streetName, town, county, postcode)

    fun generateRequirement(
        id: Long = IdGenerator.getAndIncrement(),
        requirementMainCategory: RequirementMainCategory? = ReferenceDataGenerator.UPW_RQMNT_MAIN_CATEGORY,
        length: Long,
        disposal: Disposal,
        softDeleted: Boolean = false
    ) = Requirement(id, requirementMainCategory, length, disposal, softDeleted)

    fun generateUpwAllocation(
        id: Long = IdGenerator.getAndIncrement(),
        details: UnpaidWorkDetails,
        project: UpwProject,
        projectAvailability: UpwProjectAvailability?,
        allocationDay: UnpaidWorkDay,
        requestedFrequency: ReferenceData?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        startTime: LocalTime,
        endTime: LocalTime,
        softDeleted: Boolean = false,
        rowVersion: Long = 1
    ) = UnpaidWorkAllocation(
        id,
        details,
        project,
        projectAvailability,
        allocationDay,
        requestedFrequency,
        startDate,
        endDate,
        startTime,
        endTime,
        softDeleted,
        rowVersion
    )

    fun generateEvent(
        id: Long = IdGenerator.getAndIncrement(),
        eventNumber: String,
        ftcCount: Long = 0,
        breachEnd: LocalDate? = null,
        person: Person,
        disposal: Disposal?
    ) = Event(
        id = id,
        number = eventNumber,
        ftcCount = ftcCount,
        breachEnd = breachEnd,
        person = person,
        disposal = disposal
    )
}