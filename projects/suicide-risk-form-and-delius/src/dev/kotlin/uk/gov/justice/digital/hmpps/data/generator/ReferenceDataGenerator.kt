package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.ReferenceData

object ReferenceDataGenerator {
    val REGISTER_LEVEL_DATASET = generateDataset("REGISTER LEVEL", IdGenerator.getAndIncrement())

    val HIGH_RISK_REGISTER_LEVEL = generateReferenceData(
        dataset = REGISTER_LEVEL_DATASET,
        code = "1",
        description = "High Risk",
        selectable = true,
        id = IdGenerator.getAndIncrement()
    )

    val REGISTER_TYPE_FLAG_DATASET = generateDataset("REGISTER TYPE FLAG", IdGenerator.getAndIncrement())

    val SAFEGUARDING_FLAG = generateReferenceData(
        dataset = REGISTER_TYPE_FLAG_DATASET,
        code = "3",
        description = "Safeguarding",
        selectable = true,
        id = IdGenerator.getAndIncrement()
    )

    val INFORMATION_FLAG = generateReferenceData(
        dataset = REGISTER_TYPE_FLAG_DATASET,
        code = "4",
        description = "Information",
        selectable = true,
        id = IdGenerator.getAndIncrement()
    )

    val APPOINTMENT_CONTACT_TYPE = generateContactType("APPT1", attendanceContact = true)
    val APPOINTMENT_OUTCOME = generateContactOutcome("AOUT")

    fun generateDataset(code: String, id: Long = IdGenerator.getAndIncrement()) = Dataset(code, id)

    fun generateReferenceData(
        dataset: Dataset,
        code: String,
        description: String = "Description of $code",
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(
        id = id,
        code = code,
        description = description,
        dataset = dataset,
        selectable = selectable
    )

    fun generateContactType(
        code: String,
        id: Long = IdGenerator.getAndIncrement(),
        description: String = "Description of $code",
        attendanceContact: Boolean = false,
    ) = ContactType(
        id = id,
        code = code,
        description = description,
        attendanceContact = attendanceContact
    )

    fun generateContactOutcome(
        code: String,
        id: Long = IdGenerator.getAndIncrement(),
        description: String = "Description of $code",
        enforceable: Boolean = false,
    ) = ContactOutcome(
        id = id,
        code = code,
        description = description,
        enforceable = enforceable
    )
}