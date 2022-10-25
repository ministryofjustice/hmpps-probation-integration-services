package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationDeliveryUnit

object ProbationAreaGenerator {
    val DEFAULT = ProbationArea(id = IdGenerator.getAndIncrement())
    val WITHOUT_PDU = ProbationArea(id = IdGenerator.getAndIncrement())
    val PDU = ProbationDeliveryUnit(id = IdGenerator.getAndIncrement(), probationArea = DEFAULT)
    val APPROVED_PREMISES_LDU_1 = LocalDeliveryUnit(
        id = IdGenerator.getAndIncrement(),
        description = "Approved Premises",
        probationDeliveryUnit = PDU
    )
    val APPROVED_PREMISES_LDU_2 = LocalDeliveryUnit(
        id = IdGenerator.getAndIncrement(),
        description = "THIS ALSO RELATES TO AN APPROVED PREMISES",
        probationDeliveryUnit = PDU
    )
    val NON_APPROVED_PREMISES_LDU = LocalDeliveryUnit(
        id = IdGenerator.getAndIncrement(),
        description = "Something else",
        probationDeliveryUnit = PDU
    )
}
