package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.DetailsGenerator.generatePerson
import java.time.LocalDate.now

object SearchGenerator {
    val JOHN_DOE = generatePerson("S123456", "John", "Doe", now().minusYears(28), "S3477CH", "1964/8284523P")

    val JOHN_SMITH_1 = generatePerson("S223456", "John", "Smith", nomsId = "S3478CH")
    val JOHN_SMITH_2 = generatePerson("S223457", "John", "Smith", nomsId = "S3479CH")
}