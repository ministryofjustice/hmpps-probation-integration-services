package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ReferenceData

object ReferenceDataGenerator {
    val ADDRESS_STATUS = generate("M", "Main")
    val DTR_SUB_TYPE = generate("SUB", "SUB")

    fun generate(code: String, description: String = "Description of $code", id: Long = IdGenerator.getAndIncrement()) =
        ReferenceData(code, description, id)
}
