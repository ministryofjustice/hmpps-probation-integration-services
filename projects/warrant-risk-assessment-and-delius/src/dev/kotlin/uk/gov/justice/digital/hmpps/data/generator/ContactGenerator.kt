package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Contact
import uk.gov.justice.digital.hmpps.entity.ContactType
import java.time.LocalDate

object ContactGenerator {
    val LAST_HOME_VISIT = generate(
        personId = PersonGenerator.DEFAULT.id,
        type = ContactTypeGenerator.HOME_VISIT_TO_CASE_NS,
        date = LocalDate.of(2025, 3, 17),
    )

    val HOME_VISIT_PERSON_LATEST = generate(
        personId = PersonGenerator.HOME_VISIT.id,
        type = ContactTypeGenerator.HOME_VISIT_TO_CASE_NS,
        date = LocalDate.of(2026, 1, 1),
    )

    val HOME_VISIT_PERSON_OLDER = generate(
        personId = PersonGenerator.HOME_VISIT.id,
        type = ContactTypeGenerator.HOME_VISIT_TO_CASE_NS,
        date = LocalDate.of(2025, 1, 1),
    )

    val HOME_VISIT_PERSON_SOFT_DELETED = generate(
        personId = PersonGenerator.HOME_VISIT.id,
        type = ContactTypeGenerator.HOME_VISIT_TO_CASE_NS,
        date = LocalDate.of(2026, 2, 1),
        softDeleted = true,
    )

    val HOME_VISIT_PERSON_FUTURE = generate(
        personId = PersonGenerator.HOME_VISIT.id,
        type = ContactTypeGenerator.INITIAL_APPOINTMENT_HOME_VISIT_NS,
        date = LocalDate.of(2099, 1, 1),
    )

    fun generate(
        personId: Long,
        type: ContactType,
        date: LocalDate,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement(),
    ) = Contact(
        id = id,
        personId = personId,
        type = type,
        date = date,
        softDeleted = softDeleted,
    )
}
