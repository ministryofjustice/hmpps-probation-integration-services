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

    fun generate(crn: String, mobileNumber: String?) = Person(
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
    ).also {
        it.set(
            "events", listOf(
                Event(
                    id = id(),
                    person = it,
                    active = true,
                    softDeleted = false,
                )
            )
        )
        it.set(
            "manager", ManagerEntity(
                id = id(),
                person = it,
                staff = StaffGenerator.TEST_STAFF,
                team = TeamGenerator.DEFAULT,
                active = true,
                softDeleted = false
            )
        )
    }
}
