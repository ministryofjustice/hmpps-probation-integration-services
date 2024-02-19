package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity.NsiType

object NsiTypeGenerator {
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = NsiType(id, code, "description of $code")
}

object NsiStatusGenerator {
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = NsiStatus(id, code)
}
