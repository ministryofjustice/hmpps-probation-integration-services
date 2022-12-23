package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.provider.entity.Staff

object StaffGenerator {
    val DEFAULT = generate("N54A999")

    fun generate(code: String) = Staff(IdGenerator.getAndIncrement(), code)
}
