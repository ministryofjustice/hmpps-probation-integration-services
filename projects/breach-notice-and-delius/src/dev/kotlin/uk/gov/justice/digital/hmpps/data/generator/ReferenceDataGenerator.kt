package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.*

object ReferenceDataGenerator {

    fun generateDataset(code: String, id: Long = IdGenerator.getAndIncrement()) = Dataset(code, id)

    fun generateReferenceData(
        dataset: Dataset,
        code: String,
        description: String = "Description of $code",
        selectable: Boolean = true,
        linkedData: Set<ReferenceData> = setOf(),
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(code, description, dataset, selectable, linkedData, id)

    fun generateContactType(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(code, description, id)

    fun generateContactOutcome(
        code: String,
        description: String = "Description of $code",
        enforceable: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactOutcome(code, description, enforceable, id)

    fun generateRequirementMainCategory(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = RequirementMainCategory(code, description, id)
}