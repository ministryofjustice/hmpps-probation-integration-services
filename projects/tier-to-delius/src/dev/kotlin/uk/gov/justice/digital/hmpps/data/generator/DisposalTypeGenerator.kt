package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.entity.DisposalType

object DisposalTypeGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = DisposalType(id, "SP")
}
