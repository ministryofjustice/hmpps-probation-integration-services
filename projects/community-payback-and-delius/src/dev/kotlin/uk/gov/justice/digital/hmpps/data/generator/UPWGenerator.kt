package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
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
        addressNumber = "1001",
        streetName = "Office Street",
        town = "City",
        postcode = "ZY98XW"
    )

    val DEFAULT_UPW_PROJECT = generateUpwProject(
        name = "Default UPW Project",
        code = "N01DEFAULT",
        teamId = TeamGenerator.DEFAULT_UPW_TEAM.id,
        placementAddress = DEFAULT_ADDRESS,
        projectType = ReferenceDataGenerator.GROUP_PLACEMENT_PROJECT_TYPE
    )
    val SECOND_UPW_PROJECT = generateUpwProject(
        name = "Second UPW Project",
        code = "N01SECOND",
        teamId = TeamGenerator.DEFAULT_UPW_TEAM.id,
        placementAddress = DEFAULT_ADDRESS,
        projectType = ReferenceDataGenerator.INDIVIDUAL_PLACEMENT_PROJECT_TYPE
    )

    val DEFAULT_UPW_PROJECT_AVAILABILITY = generateUpwProjectAvailability(
        upwProjectId = DEFAULT_UPW_PROJECT.id
    )
    val SECOND_UPW_PROJECT_AVAILABILITY = generateUpwProjectAvailability(
        upwProjectId = SECOND_UPW_PROJECT.id
    )

    val DEFAULT_DISPOSAL = generateDisposal(length = 12, disposalType = ReferenceDataGenerator.DEFAULT_DISPOSAL_TYPE)
    val SECOND_DISPOSAL = generateDisposal(length = 12, disposalType = ReferenceDataGenerator.DEFAULT_DISPOSAL_TYPE)

    val DEFAULT_UPW_DETAILS = generateUpwDetails(disposalId = DEFAULT_DISPOSAL.id)
    val SECOND_UPW_DETAILS = generateUpwDetails(disposalId = SECOND_DISPOSAL.id)
    val THIRD_UPW_DETAILS = generateUpwDetails(disposalId = SECOND_DISPOSAL.id)

    val DEFAULT_CONTACT =
        generateContact(
            contactTypeId = ReferenceDataGenerator.UPW_APPOINTMENT_TYPE.id,
            latestEnforcementAction = ReferenceDataGenerator.ROM_ENFORCEMENT_ACTION,
            contactOutcome = ReferenceDataGenerator.FAILED_TO_ATTEND_CONTACT_OUTCOME,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            date = LocalDate.now(),
            personId = PersonGenerator.DEFAULT_PERSON.id!!,
            officeLocation = DEFAULT_OFFICE_LOCATION,
            staff = StaffGenerator.DEFAULT_STAFF,
            team = TeamGenerator.DEFAULT_UPW_TEAM,
            provider = ProviderGenerator.DEFAULT_PROVIDER
        )
    val CONTACT_NO_ENFORCEMENT = generateContact(
        contactTypeId = ReferenceDataGenerator.UPW_APPOINTMENT_TYPE.id,
        latestEnforcementAction = null,
        contactOutcome = ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME,
        startTime = LocalTime.of(10, 15),
        endTime = LocalTime.of(16, 30),
        date = LocalDate.now(),
        personId = PersonGenerator.DEFAULT_PERSON.id!!,
        officeLocation = DEFAULT_OFFICE_LOCATION,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        provider = ProviderGenerator.DEFAULT_PROVIDER
    )

    val DEFAULT_UPW_APPOINTMENT = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        date = LocalDate.now(),
        project = DEFAULT_UPW_PROJECT,
        details = DEFAULT_UPW_DETAILS,
        contact = DEFAULT_CONTACT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = 60L,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.EXCELLENT_WORK_QUALITY,
        behaviour = ReferenceDataGenerator.EXCELLENT_BEHAVIOUR,
        minutesCredited = 30L
    )

    val LAO_EXCLUDED_UPW_APPOINTMENT = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        date = LocalDate.now(),
        project = DEFAULT_UPW_PROJECT,
        details = DEFAULT_UPW_DETAILS,
        contact = DEFAULT_CONTACT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = 60L,
        person = PersonGenerator.EXCLUDED_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.EXCELLENT_WORK_QUALITY,
        behaviour = ReferenceDataGenerator.EXCELLENT_BEHAVIOUR,
        minutesCredited = 30L
    )

    val LAO_RESTRICTED_UPW_APPOINTMENT = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        date = LocalDate.now(),
        project = DEFAULT_UPW_PROJECT,
        details = DEFAULT_UPW_DETAILS,
        contact = DEFAULT_CONTACT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = 60L,
        person = PersonGenerator.RESTRICTED_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.EXCELLENT_WORK_QUALITY,
        behaviour = ReferenceDataGenerator.EXCELLENT_BEHAVIOUR,
        minutesCredited = 30L
    )

    val UPW_APPOINTMENT_NO_ENFORCEMENT = generateUpwAppointment(
        startTime = LocalTime.of(10, 15),
        endTime = LocalTime.of(16, 30),
        date = LocalDate.now(),
        project = DEFAULT_UPW_PROJECT,
        details = DEFAULT_UPW_DETAILS,
        contact = CONTACT_NO_ENFORCEMENT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(10, 15),
        penaltyTime = null,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.EXCELLENT_WORK_QUALITY,
        behaviour = ReferenceDataGenerator.EXCELLENT_BEHAVIOUR,
        minutesCredited = 60L,
    )

    val UPW_APPOINTMENT_NO_OUTCOME = generateUpwAppointment(
        startTime = LocalTime.of(12, 0),
        endTime = LocalTime.of(14, 0),
        date = LocalDate.now().plusDays(1),
        project = SECOND_UPW_PROJECT,
        details = SECOND_UPW_DETAILS,
        contact = CONTACT_NO_ENFORCEMENT,
        contactOutcomeTypeId = null,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(12, 0),
        penaltyTime = null,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = null,
        behaviour = null,
        minutesCredited = 0L
    )

    val SECOND_UPW_APPOINTMENT_OUTCOME_NO_ENFORCEMENT = generateUpwAppointment(
        startTime = LocalTime.of(12, 0),
        endTime = LocalTime.of(14, 0),
        date = LocalDate.now().plusDays(1),
        project = SECOND_UPW_PROJECT,
        details = THIRD_UPW_DETAILS,
        contact = CONTACT_NO_ENFORCEMENT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = null,
        person = PersonGenerator.SECOND_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.UNSATISFACTORY_WORK_QUALITY,
        behaviour = ReferenceDataGenerator.UNSATISFACTORY_BEHAVIOUR,
        minutesCredited = 10L
    )

    val UPW_APPOINTMENT_PAST = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(15, 0),
        date = LocalDate.now().minusDays(1),
        project = SECOND_UPW_PROJECT,
        details = SECOND_UPW_DETAILS,
        contact = CONTACT_NO_ENFORCEMENT,
        contactOutcomeTypeId = null,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(8, 0),
        penaltyTime = null,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = null,
        behaviour = null,
        minutesCredited = 0L
    )

    val DEFAULT_RQMNT = generateRequirement(
        length = 120,
        disposal = SECOND_DISPOSAL
    )

    fun generateUpwProject(
        id: Long = IdGenerator.getAndIncrement(),
        name: String,
        code: String,
        teamId: Long,
        placementAddress: Address?,
        projectType: ReferenceData,
        hiVisRequired: Boolean = false
    ) = UpwProject(id, name, code, teamId, placementAddress, projectType, hiVisRequired)

    fun generateUpwProjectAvailability(
        id: Long = IdGenerator.getAndIncrement(),
        upwProjectId: Long
    ) = UpwProjectAvailability(id, upwProjectId)

    fun generateDisposal(
        id: Long = IdGenerator.getAndIncrement(),
        length: Long,
        softDeleted: Boolean = false,
        disposalType: DisposalType
    ) = Disposal(id, length, softDeleted, disposalType)

    fun generateUpwDetails(
        id: Long = IdGenerator.getAndIncrement(),
        disposalId: Long,
        softDeleted: Boolean = false
    ) = UpwDetails(id, disposalId, softDeleted)

    fun generateContact(
        id: Long = 0,
        contactTypeId: Long,
        contactOutcome: ContactOutcome?,
        latestEnforcementAction: EnforcementAction?,
        date: LocalDate,
        startTime: LocalTime?,
        endTime: LocalTime?,
        linkedContactId: Long? = null,
        personId: Long,
        eventId: Long? = null,
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
        id, contactTypeId, contactOutcome, latestEnforcementAction, date, startTime, endTime, linkedContactId,
        personId, eventId, requirementId, licenceConditionId, officeLocation, staff, team, provider, notes, sensitive,
        alertsActive, rowVersion
    )

    fun generateUpwAppointment(
        id: Long = 0,
        attended: Boolean? = true,
        complied: Boolean? = true,
        softDeleted: Boolean = false,
        startTime: LocalTime,
        endTime: LocalTime,
        date: LocalDate,
        project: UpwProject,
        details: UpwDetails,
        pickupLocation: OfficeLocation,
        pickupTime: LocalTime,
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
        rowVersion: Long = 1,
        createdDatetime: ZonedDateTime = ZonedDateTime.now(),
        lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
    ) = UpwAppointment(
        id,
        attended,
        complied,
        softDeleted,
        startTime,
        endTime,
        date,
        project,
        details,
        pickupLocation,
        pickupTime,
        penaltyTime,
        contact,
        contactOutcomeTypeId,
        person,
        staff,
        team,
        hiVisWorn,
        workedIntensively,
        workQuality,
        behaviour,
        minutesCredited,
        rowVersion,
        createdDatetime,
        lastUpdatedDatetime
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
        buildingName: String? = null,
        addressNumber: String? = null,
        streetName: String? = null,
        town: String? = null,
        county: String? = null,
        postcode: String? = null
    ) = OfficeLocation(id, buildingName, addressNumber, streetName, town, county, postcode)

    fun generateRequirement(
        id: Long = IdGenerator.getAndIncrement(),
        requirementMainCategory: RequirementMainCategory? = ReferenceDataGenerator.UPW_RQMNT_MAIN_CATEGORY,
        length: Long,
        disposal: Disposal,
        softDeleted: Boolean = false
    ) = Requirement(id, requirementMainCategory, length, disposal, softDeleted)
}