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

    val DEFAULT_DISPOSAL = generateDisposal()
    val SECOND_DISPOSAL = generateDisposal()

    val DEFAULT_UPW_DETAILS = generateUpwDetails(disposalId = DEFAULT_DISPOSAL.id)
    val SECOND_UPW_DETAILS = generateUpwDetails(disposalId = SECOND_DISPOSAL.id)
    val THIRD_UPW_DETAILS = generateUpwDetails(disposalId = SECOND_DISPOSAL.id)

    val DEFAULT_CONTACT =
        generateContact(
            latestEnforcementAction = ReferenceDataGenerator.DEFAULT_ENFORCEMENT_ACTION,
            contactOutcome = ReferenceDataGenerator.FAILED_TO_ATTEND_CONTACT_OUTCOME,
        )
    val CONTACT_NO_ENFORCEMENT = generateContact(
        latestEnforcementAction = null,
        contactOutcome = ReferenceDataGenerator.ATTENDED_COMPLIED_CONTACT_OUTCOME
    )

    val DEFAULT_UPW_APPOINTMENT = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        appointmentDate = LocalDate.now(),
        upwProjectId = DEFAULT_UPW_PROJECT.id,
        upwDetailsId = DEFAULT_UPW_DETAILS.id,
        contact = DEFAULT_CONTACT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = 60L,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.EXCELLENT_WORK_QUALITY,
        behaviour = ReferenceDataGenerator.EXCELLENT_BEHAVIOUR
    )

    val UPW_APPOINTMENT_NO_ENFORCEMENT = generateUpwAppointment(
        startTime = LocalTime.of(10, 15),
        endTime = LocalTime.of(16, 30),
        appointmentDate = LocalDate.now(),
        upwProjectId = DEFAULT_UPW_PROJECT.id,
        upwDetailsId = DEFAULT_UPW_DETAILS.id,
        contact = CONTACT_NO_ENFORCEMENT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(10, 15),
        penaltyTime = null,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.EXCELLENT_WORK_QUALITY,
        behaviour = ReferenceDataGenerator.EXCELLENT_BEHAVIOUR
    )

    val UPW_APPOINTMENT_NO_OUTCOME = generateUpwAppointment(
        startTime = LocalTime.of(12, 0),
        endTime = LocalTime.of(14, 0),
        appointmentDate = LocalDate.now().plusDays(1),
        upwProjectId = SECOND_UPW_PROJECT.id,
        upwDetailsId = SECOND_UPW_DETAILS.id,
        contact = CONTACT_NO_ENFORCEMENT,
        contactOutcomeTypeId = null,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(12, 0),
        penaltyTime = null,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = null,
        behaviour = null
    )

    val SECOND_UPW_APPOINTMENT_OUTCOME_NO_ENFORCEMENT = generateUpwAppointment(
        startTime = LocalTime.of(12, 0),
        endTime = LocalTime.of(14, 0),
        appointmentDate = LocalDate.now().plusDays(1),
        upwProjectId = SECOND_UPW_PROJECT.id,
        upwDetailsId = THIRD_UPW_DETAILS.id,
        contact = CONTACT_NO_ENFORCEMENT,
        contactOutcomeTypeId = 1L,
        pickupLocation = DEFAULT_OFFICE_LOCATION,
        pickupTime = LocalTime.of(9, 0),
        penaltyTime = null,
        person = PersonGenerator.DEFAULT_PERSON,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
        workQuality = ReferenceDataGenerator.UNSATISFACTORY_WORK_QUALITY,
        behaviour = ReferenceDataGenerator.UNSATISFACTORY_BEHAVIOUR
    )

    fun generateUpwProject(
        id: Long = IdGenerator.getAndIncrement(),
        name: String,
        code: String,
        teamId: Long,
        placementAddress: Address?,
        projectType: ReferenceData
    ) = UpwProject(id, name, code, teamId, placementAddress, projectType)

    fun generateUpwProjectAvailability(
        id: Long = IdGenerator.getAndIncrement(),
        upwProjectId: Long
    ) = UpwProjectAvailability(id, upwProjectId)

    fun generateDisposal(
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = Disposal(id, softDeleted)

    fun generateUpwDetails(
        id: Long = IdGenerator.getAndIncrement(),
        disposalId: Long
    ) = UpwDetails(id, disposalId)

    fun generateContact(
        id: Long = IdGenerator.getAndIncrement(),
        contactOutcome: ContactOutcome?,
        latestEnforcementAction: EnforcementAction?,
        notes: String? = null,
        sensitive: Boolean? = false,
        alertsActive: Boolean? = false,
        rowVersion: Long = 1,
    ) = Contact(id, contactOutcome, latestEnforcementAction, notes, sensitive, alertsActive, rowVersion)

    fun generateUpwAppointment(
        id: Long = IdGenerator.getAndIncrement(),
        attended: String = "Y",
        complied: String = "Y",
        softDeleted: Boolean = false,
        startTime: LocalTime,
        endTime: LocalTime,
        appointmentDate: LocalDate,
        upwProjectId: Long,
        upwDetailsId: Long,
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
        rowVersion: Long = 1,
        createdDatetime: ZonedDateTime = ZonedDateTime.now(),
        lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
    ) = UpwAppointment(
        id, attended, complied, softDeleted, startTime, endTime, appointmentDate,
        upwProjectId, upwDetailsId, pickupLocation, pickupTime, penaltyTime, contact, contactOutcomeTypeId,
        person, staff, team, hiVisWorn, workedIntensively, workQuality, behaviour, rowVersion, createdDatetime, lastUpdatedDatetime
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
}