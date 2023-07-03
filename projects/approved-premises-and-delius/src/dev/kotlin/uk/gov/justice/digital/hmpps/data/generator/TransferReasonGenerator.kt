package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.TransferReason

object TransferReasonGenerator {
    val NSI = generate("NSI")
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = TransferReason(id, code)
}
