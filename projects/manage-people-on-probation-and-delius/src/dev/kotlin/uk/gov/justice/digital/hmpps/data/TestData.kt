package uk.gov.justice.digital.hmpps.data

import uk.gov.justice.digital.hmpps.data.TestData.Contact.Type.DRUG_TEST_APPOINTMENT
import uk.gov.justice.digital.hmpps.data.TestData.Contact.Type.PLANNED_OFFICE_VISIT
import uk.gov.justice.digital.hmpps.data.TestData.Person.PERSON_1
import uk.gov.justice.digital.hmpps.data.TestData.Person.PERSON_2
import uk.gov.justice.digital.hmpps.data.TestData.Users.STAFF
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.entity.caseload.Caseload
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.entity.contact.ContactOutcome
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.entity.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.entity.event.requirement.RequirementMainCategory
import uk.gov.justice.digital.hmpps.entity.user.*
import java.time.LocalDate
import java.time.LocalTime

object TestData {
    object Person {
        val PERSON_1 = Person(id(), "X000001", "Test", "Test", "Test", "Test")
        val PERSON_2 = Person(id(), "X000002", "Test", null, null, "Test")
    }

    object Users {
        val STAFF = Staff(id(), "N01ABCD", "Test", "Staff")
        val USER = User(id(), "test.user", "Test", "User", STAFF)
        val USER_WITHOUT_STAFF = User(id(), "no-staff", "Test", "User", null)
    }

    object Teams {
        val DEFAULT = Team(
            id = id(),
            code = "N01ABC",
            description = "Test Team",
            staff = listOf(STAFF),
            provider = Provider.DEFAULT,
            startDate = LocalDate.of(2020, 1, 1),
        )
    }

    object Provider {
        val DEFAULT = Provider(id(), "N01", "Test Provider")
    }

    object OfficeLocation {
        val DEFAULT = OfficeLocation(id(), "Test Office")
    }

    object Caseload {
        val CASELOAD_FOR_PERSON_1 = generate(Person.PERSON_1, STAFF, Teams.DEFAULT)
        val CASELOAD_FOR_PERSON_2 = generate(Person.PERSON_2, STAFF, Teams.DEFAULT)

        fun generate(person: uk.gov.justice.digital.hmpps.entity.Person, staff: Staff, team: Team) = Caseload(
            id = id(),
            person = person,
            staff = staff,
            team = team,
            roleCode = "OM"
        )
    }

    object Contact {
        object Type {
            val PLANNED_OFFICE_VISIT = ContactType(
                id = id(),
                code = "COAP",
                description = "Planned office visit",
                attendance = true,
                outcomeRequired = true,
            )
            val DRUG_TEST_APPOINTMENT = ContactType(
                id = id(),
                code = "DRG",
                description = "Drug test",
                attendance = true,
                outcomeRequired = true,
            )
            val EMAIL_TEXT_FROM_POP = ContactType(
                id = id(),
                code = "CMOA",
                description = "Email/text from PoP",
                attendance = false,
                outcomeRequired = false,
            )
        }

        object Outcome {
            val COMPLIED = ContactOutcome(
                id = id(),
                code = "ATTC",
                description = "Attended - Complied",
                attended = true,
                complied = true,
            )
            val NOT_COMPLIED = ContactOutcome(
                id = id(),
                code = "FTC",
                description = "Failed to comply",
                attended = true,
                complied = false,
            )
        }

        val UPCOMING_1 = Contact(
            id = id(),
            person = PERSON_1,
            staff = STAFF,
            type = PLANNED_OFFICE_VISIT,
            location = OfficeLocation.DEFAULT,
            date = LocalDate.of(2030, 1, 1),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(9, 30)
        )

        val UPCOMING_2 = Contact(
            id = id(),
            person = PERSON_2,
            staff = STAFF,
            type = DRUG_TEST_APPOINTMENT,
            location = null,
            requirement = Requirement.RAR,
            date = LocalDate.of(2030, 1, 1),
            startTime = LocalTime.of(10, 0),
            endTime = LocalTime.of(10, 30)
        )

        val REQUIRING_OUTCOME = List(6) {
            Contact(
                id = id(),
                person = PERSON_2,
                staff = STAFF,
                type = PLANNED_OFFICE_VISIT,
                date = LocalDate.of(2025, 1, it + 1),
                startTime = LocalTime.of(9, 0)
            )
        }

        val WITH_OUTCOME = List(2) {
            Contact(
                id = id(),
                person = PERSON_1,
                staff = STAFF,
                type = PLANNED_OFFICE_VISIT,
                date = LocalDate.of(2024, 1, it + 1),
                startTime = LocalTime.of(14, 0),
                outcome = Outcome.COMPLIED
            )
        }
    }

    object Requirement {
        object Category {
            val RAR = RequirementMainCategory(id(), "F")
            val UPW = RequirementMainCategory(id(), "W")
        }

        val RAR = Requirement(id(), Category.RAR)
        val UNPAID_WORK = Requirement(id(), Category.UPW)
    }
}
