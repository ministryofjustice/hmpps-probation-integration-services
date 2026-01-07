package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.Event
import uk.gov.justice.digital.hmpps.entity.ManagerEntity
import uk.gov.justice.digital.hmpps.entity.Person

object PersonGenerator {
    val PERSON1 = generate("A000001", "07000000001")
    val PERSON2_DUPLICATE = generate("A000002", "07000000002")
    val PERSON3_DUPLICATE = generate("A000003", "07000000002")
    val PERSON4_INVALID = generate("A000004", "07000000004 invalid")
    val PERSON5_INVALID = generate("A000005", "070000005")
    val PERSON6_EMPTY = generate("A000006", null)
    val PERSON7_NO_EVENT = generate("A000007", "07000000007")

    fun generate(
        crn: String,
        mobileNumber: String?,
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
    )

    fun Person.event() = Event(id = id(), person = this)

    fun Person.manager() = ManagerEntity(
        id = id(),
        person = this,
        staff = StaffGenerator.TEST_STAFF,
        team = TeamGenerator.DEFAULT,
    )
}
