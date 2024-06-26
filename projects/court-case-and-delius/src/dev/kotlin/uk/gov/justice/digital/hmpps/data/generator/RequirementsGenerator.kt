package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENT_SENTENCE
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.Requirement
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

object RequirementsGenerator {

    private val createdDateTime = ZonedDateTime.of(LocalDate.of(2023, 12, 31), LocalTime.NOON, EuropeLondon)

    val ACTIVE_REQ = Requirement(
        IdGenerator.getAndIncrement(),
        CURRENT_SENTENCE.id,
        "notes",
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 1, 2),
        LocalDate.of(2024, 1, 3),
        LocalDate.of(2024, 1, 4),
        LocalDate.of(2024, 1, 5),
        createdDateTime,
        ReferenceDataGenerator.REQUIREMENT_SUB_CAT,
        ReferenceDataGenerator.REQUIREMENT_MAIN_CAT,
        ReferenceDataGenerator.AD_REQUIREMENT_MAIN_CAT,
        ReferenceDataGenerator.AD_REQUIREMENT_SUB_CAT,
        ReferenceDataGenerator.TERMINATION_REASON,
        3,
        active = true
    )

    val INACTIVE_REQ = generate(CURRENT_SENTENCE.id, active = false, softDeleted = false)

    val DELETED_REQ = generate(CURRENT_SENTENCE.id, active = true, softDeleted = true)

    val INACTIVE_AND_DELETED_REQ = generate(CURRENT_SENTENCE.id, active = false, softDeleted = true)

    fun generate(
        disposalId: Long,
        active: Boolean,
        softDeleted: Boolean,
    ) = Requirement(
        IdGenerator.getAndIncrement(),
        disposalId,
        createdDatetime = createdDateTime,
        active = active,
        softDeleted = softDeleted,
    )
}