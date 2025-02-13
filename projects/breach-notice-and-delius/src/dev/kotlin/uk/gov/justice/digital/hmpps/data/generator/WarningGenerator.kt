package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.WarningType

object WarningGenerator {
    val WARNING_TYPES = listOf(
        generateWarningType("WARN1"),
        generateWarningType("WARN2", selectable = false),
        generateWarningType("WARN3")
    )

    fun generateWarningType(
        code: String,
        description: String = "Description of $code",
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = WarningType(code, description, selectable, id)
}