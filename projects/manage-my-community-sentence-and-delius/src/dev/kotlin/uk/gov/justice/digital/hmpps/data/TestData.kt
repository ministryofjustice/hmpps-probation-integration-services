package uk.gov.justice.digital.hmpps.data

import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.COMMUNITY_ORDER
import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.LICENCE_CONDITION_CATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.LICENCE_CONDITION_SUBCATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.RAR_REQUIREMENT_CATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.RAR_REQUIREMENT_SUBCATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.REQUIREMENT_CATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.REQUIREMENT_SUBCATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.UPW_REQUIREMENT_CATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.ReferenceData.UPW_REQUIREMENT_SUBCATEGORY
import uk.gov.justice.digital.hmpps.data.TestData.RequirementData.RAR_REQUIREMENT
import uk.gov.justice.digital.hmpps.data.TestData.SentenceData.DISPOSAL
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.PersonalContact
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.address.OfficeLocation
import uk.gov.justice.digital.hmpps.entity.address.PersonAddress
import uk.gov.justice.digital.hmpps.entity.appointment.Contact
import uk.gov.justice.digital.hmpps.entity.appointment.ContactType
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.DisposalType
import uk.gov.justice.digital.hmpps.entity.sentence.Event
import uk.gov.justice.digital.hmpps.entity.sentence.NonStatutoryIntervention
import uk.gov.justice.digital.hmpps.entity.sentence.licencecondition.LicenceCondition
import uk.gov.justice.digital.hmpps.entity.sentence.licencecondition.LicenceConditionMainCategory
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.Requirement
import uk.gov.justice.digital.hmpps.entity.sentence.requirement.RequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.staff.CommunityManager
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.StaffUser
import uk.gov.justice.digital.hmpps.entity.staff.Team
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkDetails
import java.time.LocalDate
import java.time.LocalTime

object TestData {
    object ReferenceData {
        val PREV_ADDRESS_STATUS = ReferenceData(id(), "P", "Previous Address")
        val MAIN_ADDRESS_STATUS = ReferenceData(id(), PersonAddress.MAIN_ADDRESS_STATUS, "Main Address")
        val EMERGENCY_CONTACT_TYPE = ReferenceData(id(), PersonalContact.EMERGENCY_CONTACT, "Emergency Contact")
        val HOURS = ReferenceData(id(), "H", "Hours")
        val DAYS = ReferenceData(id(), "D", "Days")
        val APPOINTMENT_CONTACT_TYPE = ContactType(id(), "Office Appointment", true)
        val NON_APPOINTMENT_CONTACT_TYPE = ContactType(id(), "Case Note", false)
        val COMMUNITY_ORDER = DisposalType(id(), "Community Order")
        val REQUIREMENT_CATEGORY = RequirementMainCategory(id(), "C", "Court - Accredited Programme", DAYS)
        val REQUIREMENT_SUBCATEGORY = ReferenceData(id(), "BC", "Building Choices")
        val UPW_REQUIREMENT_CATEGORY = RequirementMainCategory(id(), Requirement.UPW, "Unpaid Work", HOURS)
        val UPW_REQUIREMENT_SUBCATEGORY = ReferenceData(id(), "UPW", "Regular")
        val RAR_REQUIREMENT_CATEGORY =
            RequirementMainCategory(id(), Requirement.RAR, "Rehabilitation Activity Requirement (RAR)", DAYS)
        val RAR_REQUIREMENT_SUBCATEGORY = ReferenceData(id(), "RAR", "Rehabilitation Activity Requirement (RAR)")
        val LICENCE_CONDITION_CATEGORY =
            LicenceConditionMainCategory(id(), "Alcohol Monitoring (Electronic Monitoring)")
        val LICENCE_CONDITION_SUBCATEGORY = ReferenceData(
            id = id(),
            code = "ALC",
            description = "You must not drink any alcohol until [END DATE]. You will need to wear an electronic tag all the time so we can check this."
        )
    }

    object TeamData {
        val OFFICE = OfficeLocation(
            id = id(),
            code = "N01OFF1",
            description = "Test Office",
            buildingName = "Building name",
            buildingNumber = "123",
            streetName = "High Street",
            town = "Test Town",
            district = "Test District",
            county = "Test County",
            postcode = "TE1 1ST"
        )
        val TEAM = Team(id(), "N01ABC", "Test Team", listOf(OFFICE))
    }

    object StaffData {
        val STAFF = Staff(id(), "N01ABCD", "Test", "Staff", StaffUser(id(), "test.user"))
        val STAFF_WITHOUT_USER = Staff(id(), "N01DCBA", "Test", "NoUser")
    }

    object PersonData {
        val DEFAULT = Person(
            id = id(),
            crn = "X000001",
            firstName = "Test",
            secondName = "Middle",
            thirdName = "Name",
            surname = "One",
            preferredName = "Tester",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            telephoneNumber = "01000000001",
            mobileNumber = "07111111111",
            emailAddress = "person.one@example.com",
            manager = CommunityManager(id(), StaffData.STAFF, TeamData.TEAM),
        )

        val BASIC = Person(
            id = id(),
            crn = "X000002",
            firstName = "Test",
            surname = "Two",
            dateOfBirth = LocalDate.of(1980, 1, 1),
            mobileNumber = "07222222222",
            manager = CommunityManager(id(), StaffData.STAFF_WITHOUT_USER, TeamData.TEAM),
        )
    }

    object AddressData {
        val MAIN_ADDRESS = PersonAddress(
            id = id(),
            person = PersonData.DEFAULT,
            status = ReferenceData.MAIN_ADDRESS_STATUS,
            addressNumber = "1",
            buildingName = "My Building",
            streetName = "My Street",
            town = "My Town",
            district = "My District",
            county = "My County",
            postcode = "TE1 1ST"
        )
        val PREV_ADDRESS = PersonAddress(
            id = id(),
            person = PersonData.DEFAULT,
            status = ReferenceData.PREV_ADDRESS_STATUS,
            noFixedAbode = true,
            postcode = "NF1 1NF",
        )
    }

    object EmergencyContactData {
        val EMERGENCY_CONTACT = PersonalContact(
            id = id(),
            person = PersonData.DEFAULT,
            type = ReferenceData.EMERGENCY_CONTACT_TYPE,
            firstName = "Joe",
            surname = "Bloggs",
            relationship = "Sister",
            mobileNumber = "07333333333",
            emailAddress = "joe.bloggs@example.com",
            startDate = LocalDate.of(2020, 1, 1),
        )
    }

    object SentenceData {
        val EVENT = Event(
            id = id(),
            number = "1",
            personId = PersonData.DEFAULT.id,
        )
        val DISPOSAL = Disposal(
            id = id(),
            event = EVENT,
            type = COMMUNITY_ORDER,
            date = LocalDate.of(2024, 1, 1),
            expectedEndDate = LocalDate.of(2025, 1, 1),
            enteredExpectedEndDate = LocalDate.of(2025, 6, 1),
        )
    }

    object RequirementData {
        val REQUIREMENT =
            Requirement(id(), length = null, DISPOSAL, REQUIREMENT_CATEGORY, REQUIREMENT_SUBCATEGORY)
        val UPW_REQUIREMENT =
            Requirement(id(), length = 10, DISPOSAL, UPW_REQUIREMENT_CATEGORY, UPW_REQUIREMENT_SUBCATEGORY)
        val RAR_REQUIREMENT =
            Requirement(id(), length = 15, DISPOSAL, RAR_REQUIREMENT_CATEGORY, RAR_REQUIREMENT_SUBCATEGORY)
    }

    object LicenceConditionData {
        val LICENCE_CONDITION = LicenceCondition(
            id = id(),
            startDate = LocalDate.of(2024, 1, 1),
            commencementDate = LocalDate.of(2024, 2, 1),
            expectedEndDate = LocalDate.of(2025, 1, 1),
            disposal = DISPOSAL,
            mainCategory = LICENCE_CONDITION_CATEGORY,
            subCategory = LICENCE_CONDITION_SUBCATEGORY
        )
    }

    object UnpaidWorkData {
        val UNPAID_WORK_DETAILS = UnpaidWorkDetails(id(), DISPOSAL.id)
        val UNPAID_WORK_APPOINTMENT = UnpaidWorkAppointment(id(), UNPAID_WORK_DETAILS, minutesCredited = 180)
    }

    object RarData {
        val RAR_NSI = NonStatutoryIntervention(id(), RAR_REQUIREMENT.id)
        val RAR_CONTACT_1 = generate(LocalDate.of(2024, 1, 3), LocalTime.of(9, 0), LocalTime.of(10, 0))
        val RAR_CONTACT_2 = generate(LocalDate.of(2024, 1, 4), LocalTime.of(11, 0), LocalTime.of(12, 0))
        val RAR_CONTACT_3_SAME_DAY = generate(LocalDate.of(2024, 1, 4), LocalTime.of(15, 0), LocalTime.of(16, 0))
        val RAR_CONTACT_NOT_ATTENDED = generate(
            LocalDate.of(2024, 1, 5), LocalTime.of(11, 0), LocalTime.of(12, 0),
            attended = false, complied = false,
        )

        fun generate(
            date: LocalDate,
            startTime: LocalTime,
            endTime: LocalTime,
            description: String? = null,
            type: ContactType = ReferenceData.NON_APPOINTMENT_CONTACT_TYPE,
            attended: Boolean? = true,
            complied: Boolean? = true,
        ) = Contact(
            id = id(),
            personId = PersonData.DEFAULT.id,
            date = date,
            startTime = startTime,
            endTime = endTime,
            type = type,
            description = description,
            nsiId = RAR_NSI.id,
            staff = StaffData.STAFF,
            attended = attended,
            complied = complied,
            rarActivity = true,
        )
    }

    object AppointmentData {
        val FUTURE_1 = generate(
            LocalDate.of(2050, 1, 1), LocalTime.of(9, 0), LocalTime.of(9, 30),
            description = "Future appointment",
        )
        val FUTURE_2 = generate(
            LocalDate.of(2050, 1, 1), LocalTime.of(12, 0), LocalTime.of(13, 30),
            description = "Future appointment - no location",
            location = null
        )
        val NON_APPOINTMENT = generate(
            LocalDate.of(2050, 1, 1), LocalTime.of(10, 0), LocalTime.of(10, 30),
            type = ReferenceData.NON_APPOINTMENT_CONTACT_TYPE,
            description = "Non attendance contact",
        )
        val PAST_1 = generate(
            LocalDate.of(2020, 1, 1), LocalTime.of(15, 0), LocalTime.of(15, 30),
            description = "Past appointment - attended",
            attended = true, complied = true,
        )
        val PAST_2 = generate(
            LocalDate.of(2020, 1, 1), LocalTime.of(10, 0), LocalTime.of(10, 45),
            description = "Past appointment - not attended",
            attended = false, complied = false,
        )

        fun generate(
            date: LocalDate,
            startTime: LocalTime,
            endTime: LocalTime,
            description: String? = null,
            type: ContactType = ReferenceData.APPOINTMENT_CONTACT_TYPE,
            location: OfficeLocation? = TeamData.OFFICE,
            attended: Boolean? = null,
            complied: Boolean? = null,
        ) = Contact(
            id = id(),
            personId = PersonData.DEFAULT.id,
            date = date,
            startTime = startTime,
            endTime = endTime,
            type = type,
            description = description,
            location = location,
            staff = StaffData.STAFF,
            attended = attended,
            complied = complied,
        )
    }
}
