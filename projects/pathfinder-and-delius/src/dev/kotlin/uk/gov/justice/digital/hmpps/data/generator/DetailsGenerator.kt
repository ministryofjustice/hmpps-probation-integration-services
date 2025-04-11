package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.*
import java.time.LocalDate

object DetailsGenerator {
    val RELIGION = ReferenceData(
        IdGenerator.getAndIncrement(),
        "Jedi",
        "Jedi"
    )
    val NATIONALITY = ReferenceData(
        IdGenerator.getAndIncrement(),
        "BRITISH",
        "BRITISH"
    )

    val MALE = ReferenceData(
        IdGenerator.getAndIncrement(),
        "MALE",
        "MALE"
    )

    val FEMALE = ReferenceData(
        IdGenerator.getAndIncrement(),
        "FEMALE",
        "FEMALE"
    )

    val PERSON = DetailPerson(
        IdGenerator.getAndIncrement(),
        "X012773",
        "1231235",
        "1111111111111",
        MALE,
        RELIGION,
        NATIONALITY,
        null,
        listOf(),
        listOf(),
        LocalDate.now().minusYears(18),
        "Smith",
        "Bob",
        "Richard",
        "Clive",
        true
    )

    val ALIAS_1 = PersonAlias(
        PERSON,
        "Phil",
        "Jack",
        "Paul",
        "Brown",
        LocalDate.of(1977, 12, 8),
        MALE,
        false,
        IdGenerator.getAndIncrement(),
    )

    val ALIAS_2 = PersonAlias(
        PERSON,
        "Phillis",
        "Jackie",
        "Paula",
        "Brown",
        LocalDate.of(1976, 5, 18),
        FEMALE,
        false,
        IdGenerator.getAndIncrement(),
    )
    val INSTITUTION = Institution(IdGenerator.getAndIncrement(), "HMP-LDN")

    val RELEASE_TYPE = ReferenceData(IdGenerator.getAndIncrement(), "RSN1", "Release reason")

    val RELEASE = DetailRelease(
        IdGenerator.getAndIncrement(),
        KeyDateGenerator.CUSTODY.id,
        INSTITUTION,
        null,
        releaseType = RELEASE_TYPE,
        LocalDate.now()
    )

    val RECALL_REASON = RecallReason(IdGenerator.getAndIncrement(), "REC1", "Recall reason")

    val RECALL = Recall(
        IdGenerator.getAndIncrement(),
        RELEASE,
        LocalDate.now(),
        RECALL_REASON
    )

    val DEFAULT_PA = DetailProbationArea(true, "London", "LDN", IdGenerator.getAndIncrement())
    val DISTRICT = DetailDistrict(true, "KK", "Kings Cross", IdGenerator.getAndIncrement())
    val STAFF = DetailStaff("LNDMCDS", "Simon", "Smith", "James", IdGenerator.getAndIncrement())
    val TEAM = Team(IdGenerator.getAndIncrement(), "LNDMCD", "Description of LNCMCD", DEFAULT_PA, DISTRICT)

    val PERSON_MANAGER = PersonManager(IdGenerator.getAndIncrement(), PERSON, DEFAULT_PA, STAFF, TEAM)

    fun generatePerson(
        crn: String,
        forename: String,
        surname: String,
        dob: LocalDate = LocalDate.now().minusYears(27),
        nomsId: String? = null,
        pnc: String? = null,
        gender: ReferenceData = listOf(MALE, FEMALE).random(),
        id: Long = IdGenerator.getAndIncrement()
    ) = DetailPerson(
        id,
        crn,
        nomsId,
        pnc,
        gender,
        RELIGION,
        NATIONALITY,
        null,
        listOf(),
        listOf(),
        dob,
        surname,
        forename,
        null,
        null,
        false
    )

    fun generatePersonManager(person: DetailPerson, id: Long = IdGenerator.getAndIncrement()) =
        PersonManager(id, person, DEFAULT_PA, STAFF, TEAM)
}
