package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.ManagerEntity
import uk.gov.justice.digital.hmpps.entity.Person
import uk.gov.justice.digital.hmpps.set

object PersonGenerator {
    val PERSON1 = generate("A000001", "07000000001")
    val PERSON2 = generate("A000002", "07000000002")
    val PERSON3 = generate("A000003", "07000000002")
    val PERSON4 = generate("A000004", "07000000004 invalid")
    val PERSON5 = generate("A000005", "070000005")
    val PERSON6 = generate("A000006", null)
    val PERSON7 = generate("A000007", "07000000007", events = emptyList())

    fun generate(
        crn: String,
        mobileNumber: String?,
        events: List<(Person) -> Event> = listOf { Event(id = id(), person = it) }
    ) = Person(
        id = id(),
        crn = crn,
        forename = "Test",
        secondName = null,
        thirdName = null,
        surname = "Person",
        mobileNumber = mobileNumber,
        manager = null,
        events = emptyList(),
        softDeleted = false
    ).also { person ->
        person.set("events", events.map { it(person) })
        person.set(
            "manager", ManagerEntity(
                id = id(),
                person = person,
                staff = StaffGenerator.TEST_STAFF,
                team = TeamGenerator.DEFAULT,
            )
        )
    }
}
