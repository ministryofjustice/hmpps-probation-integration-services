package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff

object StaffGenerator {
    val UNALLOCATED = generate("N01UATU")
    val ALLOCATED = generate("N01ABBA")
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = Staff(code, id)
}
