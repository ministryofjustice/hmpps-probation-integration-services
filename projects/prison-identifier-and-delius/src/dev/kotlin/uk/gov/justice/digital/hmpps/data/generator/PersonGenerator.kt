package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PersonGenerator {
    val PERSON_WITH_NOMS = generate("A000001", "E1234XS")
    val PERSON_WITH_NO_NOMS = generate("A000002", pncNumber = "07/220000004Q")
    val PERSON_WITH_MULTI_MATCH = generate("A000003", forename = "Jack", surname = "Jones")
    val PERSON_WITH_NO_MATCH = generate("A000004", forename = "Fred", surname = "Jones", dobString = "12/12/2001")
    val PERSON_WITH_NOMS_IN_DELIUS = generate("A000005", pncNumber = "07/220000004Q")
    val PERSON_WITH_DUPLICATE_NOMS = generate("A000006", "G5541UN")
    val PERSON_WITH_EXISTING_NOMS = generate("A000007", "A0007AA")

    fun generate(
        crn: String,
        noms: String? = null,
        pncNumber: String? = null,
        gender: ReferenceData = ReferenceDataGenerator.MALE,
        forename: String = "bob",
        surname: String = "smith",
        softDeleted: Boolean = false,
        dobString: String = "12/12/2000",
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(
        id,
        crn,
        LocalDate.parse(dobString, DateTimeFormatter.ofPattern("MM/dd/yyyy")),
        forename,
        null,
        null,
        surname,
        noms,
        null,
        null,
        pncNumber,
        gender,
        listOf(),
        softDeleted = softDeleted
    )

    fun generateEvent(person: Person, id: Long = IdGenerator.getAndIncrement()) =
        Event(id = id, person = person, active = true, softDeleted = false)

    fun generateOrderManager(event: Event, id: Long = IdGenerator.getAndIncrement()) =
        OrderManager(id = id, eventId = event.id, staffId = 9999, teamId = 9999, providerId = 9999)

    fun generateDisposal(startDate: LocalDate, event: Event, id: Long = IdGenerator.getAndIncrement()) =
        Disposal(id, startDate, event, active = true, softDeleted = false)

    fun generateCustody(disposal: Disposal, id: Long = IdGenerator.getAndIncrement()) =
        Custody(id, null, status = ReferenceDataGenerator.CUSTODY_STATUS, disposal = disposal)
}

