package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiStatus
import uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.NsiType

object NsiTypeGenerator {
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = NsiType(id, code)
}

object NsiStatusGenerator {
    fun generate(code: String, id: Long = IdGenerator.getAndIncrement()) = NsiStatus(id, code)
}
