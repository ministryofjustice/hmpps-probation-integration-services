package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PersonGenerator {
    val PERSON_WITH_NOMS = generate("A000001", "E1234XS")
    val PERSON_WITH_NO_NOMS = generate("A000002", pncNumber = "2007/2200004Q")
    val PERSON_WITH_MULTI_MATCH = generate("A000003", forename = "Jack", surname = "Jones")
    val PERSON_WITH_NO_MATCH = generate("A000004", forename = "Fred", surname = "Jones", dobString = "12/12/2001")
    val PERSON_WITH_NOMS_IN_DELIUS = generate("A000005", pncNumber = "2007/2200004Q")
    val PERSON_WITH_DUPLICATE_NOMS = generate("A000006", "G5541UN")
    val PERSON_WITH_EXISTING_NOMS = generate("A000007", "A0007AA")

    val PERSON_WITH_NOMS_DB = generate(
        crn = "A000010",
        noms = "A0010DB",
        pncNumber = "24/0000001Y",
        croNumber = "00001/24M",
        dobString = "05/17/1961"
    )

    val PERSON_ALIAS_1 = generateAlias(
        offenderId = PERSON_WITH_NOMS_DB.id,
        forename = "terry",
        surname = "brown",
        dobString = "08/12/1962",
        surnameSoundex = "B650",
        firstNameSoundex = "T600",
    )

    val PERSON_ALIAS_2 = generateAlias(
        offenderId = PERSON_WITH_NOMS_DB.id,
        forename = "arthur",
        surname = "askew",
        dobString = "04/13/1969",
        surnameSoundex = "A200",
        firstNameSoundex = "A636",
    )

    val OFFENDER_MANAGER = generateOffenderManager(personId = PERSON_WITH_NOMS_DB.id)

    fun generate(
        crn: String,
        noms: String? = null,
        pncNumber: String? = null,
        gender: ReferenceData = ReferenceDataGenerator.MALE,
        forename: String = "bob",
        surname: String = "smith",
        softDeleted: Boolean = false,
        dobString: String = "12/12/2000",
        id: Long = IdGenerator.getAndIncrement(),
        immigrationNumber: String? = null,
        niNumber: String? = null,
        mostRecentPrisonerNumber: String? = null,
        croNumber: String? = null,
        surnameSoundex: String = "S530",
        firstNameSoundex: String = "B100",
    ) = Person(
        id,
        crn,
        LocalDate.parse(dobString, DateTimeFormatter.ofPattern("MM/dd/yyyy")),
        forename,
        null,
        null,
        surname,
        null,
        null,
        true,
        noms,
        immigrationNumber,
        niNumber,
        mostRecentPrisonerNumber,
        croNumber,
        pncNumber,
        gender,
        listOf(),
        surnameSoundex,
        firstNameSoundex,
        softDeleted = softDeleted
    )

    fun generateAlias(
        id: Long = IdGenerator.getAndIncrement(),
        offenderId: Long,
        forename: String,
        surname: String,
        dobString: String,
        surnameSoundex: String,
        firstNameSoundex: String,
    ) = Alias(
        id = id,
        offenderId = offenderId,
        forename = forename,
        surname = surname,
        dateOfBirth = LocalDate.parse(dobString, DateTimeFormatter.ofPattern("MM/dd/yyyy")),
        surnameSoundex = surnameSoundex,
        firstNameSoundex = firstNameSoundex,
        softDeleted = false,
    )

    fun generateEvent(person: Person, id: Long = IdGenerator.getAndIncrement()) =
        Event(id = id, person = person, active = true, softDeleted = false)

    fun generateOrderManager(event: Event, id: Long = IdGenerator.getAndIncrement()) =
        OrderManager(id = id, eventId = event.id, staffId = 9999, teamId = 9999, providerId = 9999)

    fun generateDisposal(startDate: LocalDate, event: Event, id: Long = IdGenerator.getAndIncrement()) =
        Disposal(id, startDate, event, active = true, softDeleted = false)

    fun generateCustody(disposal: Disposal, id: Long = IdGenerator.getAndIncrement()) =
        Custody(id, null, status = ReferenceDataGenerator.CUSTODY_STATUS, disposal = disposal)

    fun generateOffenderManager(
        id: Long = IdGenerator.getAndIncrement(),
        personId: Long,
        softDeleted: Boolean = false,
        active: Boolean = true
    ) = OffenderManager(id, personId, softDeleted, active)
}

