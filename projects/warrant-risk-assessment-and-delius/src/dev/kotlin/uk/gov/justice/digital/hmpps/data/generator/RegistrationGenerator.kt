package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.RegisterType
import uk.gov.justice.digital.hmpps.entity.Registration
import java.time.LocalDate

object RegistrationGenerator {
    val MAPPA_TYPE = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "M1",
        description = "MAPPA Level 1"
    )

    val MAPPA_TYPE_M2 = RegisterType(
        id = IdGenerator.getAndIncrement(),
        code = "M2",
        description = "MAPPA Level 2"
    )

    val MAPPA_REGISTRATION = Registration(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.DEFAULT.id,
        type = MAPPA_TYPE,
        date = LocalDate.of(2025, 1, 1),
        notes = "some notes in here"
    )

    val OLDER_MAPPA_REGISTRATION = Registration(
        id = IdGenerator.getAndIncrement(),
        personId = PersonGenerator.DEFAULT.id,
        type = MAPPA_TYPE_M2,
        date = LocalDate.of(2024, 6, 1),
        notes = "older registration notes"
    )
}