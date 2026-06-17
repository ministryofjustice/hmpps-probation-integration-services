package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Contact
import uk.gov.justice.digital.hmpps.entity.ContactType
import java.time.LocalDate

object ContactGenerator {
    val HOME_VISIT_TYPE = ContactType(IdGenerator.getAndIncrement(), "CHVS")

    val LAST_HOME_VISIT = generate(
        personId = PersonGenerator.DEFAULT.id,
        type = HOME_VISIT_TYPE,
        date = LocalDate.of(2025, 3, 17),
    )

    val OLDER_HOME_VISIT = generate(
        personId = PersonGenerator.DEFAULT.id,
        type = HOME_VISIT_TYPE,
        date = LocalDate.of(2024, 6, 1),
    )

    fun generate(
        personId: Long,
        type: ContactType,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Contact(
        id = id,
        personId = personId,
        type = type,
        date = date,
    )
}
