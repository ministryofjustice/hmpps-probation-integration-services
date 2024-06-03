package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData

object AddressRDGenerator {
    val CAS3_ADDRESS_TYPE = generate("A17", DatasetGenerator.ADDRESS_TYPE.id, "Approved Premises")
    val MAIN_ADDRESS_TYPE = generate("B17", DatasetGenerator.ADDRESS_TYPE.id, "Main")
    val MAIN_ADDRESS_STATUS = generate("M", DatasetGenerator.ADDRESS_STATUS.id, "Main Address")
    val PREV_ADDRESS_STATUS = generate("P", DatasetGenerator.ADDRESS_STATUS.id, "Previous Address")

    fun generate(
        code: String,
        datasetId: Long,
        description: String = "Description of $code",
        id: Long = IdGenerator.getAndIncrement()
    ) = ReferenceData(id, code, description, datasetId)
}
