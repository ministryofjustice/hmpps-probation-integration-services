package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.person.Person
import uk.gov.justice.digital.hmpps.entity.person.PersonManager
import uk.gov.justice.digital.hmpps.entity.staff.Staff
import uk.gov.justice.digital.hmpps.entity.staff.Team
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_PERSON = generatePerson(
        crn = "Z000001",
        forename = "Default",
        surname = "Person",
        dateOfBirth = LocalDate.of(1990, 6, 10)
    )

    val PERSON_2 = generatePerson(
        crn = "Z009999",
        forename = "Default",
        surname = "Person_2",
        dateOfBirth = LocalDate.of(1987, 3, 9)
    )

    val SECOND_PERSON = generatePerson(
        crn = "Z000222",
        forename = "Second",
        surname = "Person",
        dateOfBirth = LocalDate.of(1977, 1, 25)
    )

    val EXCLUDED_PERSON = generatePerson(
        crn = "E123456",
        forename = "Excluded",
        surname = "Person",
        dateOfBirth = LocalDate.of(1985, 3, 15)
    )

    val RESTRICTED_PERSON = generatePerson(
        crn = "R123456",
        forename = "Restricted",
        surname = "Person",
        dateOfBirth = LocalDate.of(1992, 8, 30)
    )

    val DEFAULT_PERSON_MANAGER = generatePersonManager(
        personId = DEFAULT_PERSON.id,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
    )

    fun generatePerson(
        id: Long = IdGenerator.getAndIncrement(),
        crn: String,
        forename: String,
        secondName: String? = null,
        thirdName: String? = null,
        surname: String,
        dateOfBirth: LocalDate
    ) = Person(id, crn, forename, secondName, thirdName, surname, dateOfBirth)

    fun generatePersonManager(
        id: Long = IdGenerator.getAndIncrement(),
        personId: Long,
        staff: Staff,
        team: Team,
        active: Boolean = true,
        softDeleted: Boolean = false
    ) = PersonManager(
        id = id,
        personId = personId,
        staff = staff,
        team = team,
        active = active,
        softDeleted = softDeleted
    )
}