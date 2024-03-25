package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData

object ReferenceDataGenerator {

    fun generate(code: String, description: String = "Description of $code", id: Long = IdGenerator.getAndIncrement()) =
        ReferenceData(code, description, id)
}