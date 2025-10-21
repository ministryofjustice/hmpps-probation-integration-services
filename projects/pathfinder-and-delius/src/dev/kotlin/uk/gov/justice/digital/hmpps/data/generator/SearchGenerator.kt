package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.MALE
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.STAFF
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.TEAM
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.generatePerson
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.PersonAlias
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.RegisterType
import uk.gov.justice.digital.hmpps.entity.Registration
import java.time.LocalDate

object SearchGenerator {
    val JOHN_DOE = generatePerson("S123456", "John", "Doe", LocalDate.of(1998, 2, 23), "S3477CH", "1964/8284523P", MALE)

    val JOHN_SMITH_1 =
        generatePerson("S223456", "John", "Smith", LocalDate.of(1998, 1, 1), nomsId = "S3478CH", gender = MALE)
    val JOHN_SMITH_2 =
        generatePerson("S223457", "John", "Smith", LocalDate.of(1998, 12, 12), nomsId = "S3479CH", gender = MALE)

    val JOHN_SMITH_1_ALIAS = PersonAlias(
        JOHN_SMITH_1,
        "Johnny",
        null,
        null,
        "Smithson",
        LocalDate.of(2000, 1, 1),
        MALE,
        false,
        IdGenerator.getAndIncrement(),
    )

    val REGISTRATION_1 = Registration(
        id = id(),
        person = JOHN_SMITH_1,
        type = RegisterType(id(), "TYPE1", "Registration type"),
        category = null,
        level = null,
        team = TEAM,
        staff = STAFF,
        date = LocalDate.of(2024, 12, 31),
        nextReviewDate = LocalDate.of(2026, 12, 31),
        notes = "Some notes"
    )

    val REGISTRATION_2 = Registration(
        id = id(),
        person = JOHN_SMITH_1,
        type = RegisterType(id(), "MAPP", "MAPPA type"),
        level = ReferenceData(id(), "M1", "MAPPA Level 1"),
        category = ReferenceData(id(), "M4", "MAPPA Category 4"),
        team = TEAM,
        staff = STAFF,
        date = LocalDate.of(2024, 12, 31),
        nextReviewDate = LocalDate.of(2026, 12, 31),
        notes = "Some notes"
    )
}