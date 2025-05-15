package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.MALE
import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.generatePerson
import uk.gov.justice.digital.hmpps.entity.PersonAlias
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
}