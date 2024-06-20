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
        RELIGION,
        NATIONALITY,
        listOf(),
        listOf(),
        LocalDate.now().minusYears(18),
        "Smith",
        "Bob",
        "Richard",
        "Clive",
        true
    )

    val ALIAS_1 = OffenderAlias(
        IdGenerator.getAndIncrement(),
        PERSON,
        LocalDate.of(1977, 12, 8),
        "Phil",
        "Jack",
        false,
        "Paul",
        "Brown",
        MALE
    )
    val ALIAS_2 = OffenderAlias(
        IdGenerator.getAndIncrement(),
        PERSON,
        LocalDate.of(1976, 5, 18),
        "Phillis",
        "Jackie",
        false,
        "Paula",
        "Brown",
        FEMALE
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
    val STAFF = DetailStaff("Simon", "Smith", "James", IdGenerator.getAndIncrement())
    val TEAM = Team(IdGenerator.getAndIncrement(), "TEAMCD", DEFAULT_PA, DISTRICT)

    val PERSON_MANAGER = PersonManager(IdGenerator.getAndIncrement(), PERSON, DEFAULT_PA, STAFF, TEAM)
}
