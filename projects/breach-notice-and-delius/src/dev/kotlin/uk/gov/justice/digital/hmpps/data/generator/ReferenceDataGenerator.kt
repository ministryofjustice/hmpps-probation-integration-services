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
        attendanceContact: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(code, description, attendanceContact, id)

    fun generateContactOutcome(
        code: String,
        description: String = "Description of $code",
        enforceable: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactOutcome(code, description, enforceable, id)

    fun generateDisposalType(
        code: String,
        sentenceType: String? = "SC",
        requiredInformation: String? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = DisposalType(code, sentenceType, requiredInformation, id)

    fun generateRequirementMainCategory(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = RequirementMainCategory(code, description, id)

    fun generatePssRequirementMainCategory(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = PssRequirementMainCategory(code, description, id)

    fun generatePssRequirementSubCategory(
        code: String,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = PssRequirementSubCategory(code, description, id)
}