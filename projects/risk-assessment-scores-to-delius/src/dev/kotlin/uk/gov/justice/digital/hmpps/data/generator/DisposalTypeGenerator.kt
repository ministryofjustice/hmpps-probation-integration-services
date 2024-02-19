package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.DisposalType

object DisposalTypeGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = DisposalType(id, "ORA Adult Custody")
}
