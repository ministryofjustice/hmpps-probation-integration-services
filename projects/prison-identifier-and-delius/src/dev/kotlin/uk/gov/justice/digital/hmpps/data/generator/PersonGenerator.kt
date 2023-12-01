package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PersonGenerator {
    val MALE = generateGender("M")
    val PERSON_WITH_NOMS = generate("A000001", "1234567")
    val PERSON_WITH_NO_NOMS = generate("A000002", pncNumber = "07/220004Q")
    val PERSON_WITH_MULTI_MATCH = generate("A000003", forename = "Jack", surname = "Jones")

    fun generate(
        crn: String,
        noms: String? = null,
        pncNumber: String? = null,
        gender: ReferenceData = MALE,
        forename: String = "bob",
        surname: String = "smith",
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) =
        Person(
            id,
            crn,
            LocalDate.parse("12/12/2000", DateTimeFormatter.ofPattern("MM/dd/yyyy")),
            forename,
            null,
            null,
            surname,
            noms,
            null,
            pncNumber,
            gender,
            softDeleted = softDeleted
        )

    fun generateEvent(person: Person, id: Long = IdGenerator.getAndIncrement()) =
        Event(id = id, person = person, active = true, softDeleted = false)

    fun generateDisposal(startDate: LocalDate, event: Event, id: Long = IdGenerator.getAndIncrement()) =
        Disposal(id, startDate, event, active = true, softDeleted = false)

    fun generateGender(code: String, id: Long = IdGenerator.getAndIncrement()) = ReferenceData(id, code)
}
