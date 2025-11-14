package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team
import java.time.LocalDate

object PersonGenerator {
    val DEFAULT_PERSON = generatePerson(
        crn = "Z000001",
        forename = "Default",
        surname = "Person",
        dateOfBirth = LocalDate.of(1990, 6, 10)
    )

    val SECOND_PERSON = generatePerson(
        crn = "Z000222",
        forename = "Second",
        surname = "Person",
        dateOfBirth = LocalDate.of(1977, 1, 25)
    )

    val DEFAULT_PERSON_MANAGER = generatePersonManager(
        personId = DEFAULT_PERSON.id!!,
        staff = StaffGenerator.DEFAULT_STAFF,
        team = TeamGenerator.DEFAULT_UPW_TEAM,
    )

    fun generatePerson(
        id: Long = IdGenerator.getAndIncrement(),
        crn: String,
        forename: String,
        secondName: String? = null,
        surname: String,
        dateOfBirth: LocalDate
    ) = Person(id, crn, forename, secondName, surname, dateOfBirth)

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