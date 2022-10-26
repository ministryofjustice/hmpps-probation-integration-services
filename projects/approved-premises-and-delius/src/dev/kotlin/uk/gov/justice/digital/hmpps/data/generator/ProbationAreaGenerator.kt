package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.LocalAdminUnit
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationDeliveryUnit

object ProbationAreaGenerator {
    val DEFAULT = ProbationArea(id = IdGenerator.getAndIncrement())
    val WITHOUT_PDU = ProbationArea(id = IdGenerator.getAndIncrement())
    val PDU = ProbationDeliveryUnit(id = IdGenerator.getAndIncrement(), probationArea = DEFAULT)
    val APPROVED_PREMISES_LAU_1 = LocalAdminUnit(
        id = IdGenerator.getAndIncrement(),
        description = "Approved Premises",
        probationDeliveryUnit = PDU
    )
    val APPROVED_PREMISES_LAU_2 = LocalAdminUnit(
        id = IdGenerator.getAndIncrement(),
        description = "THIS ALSO RELATES TO AN APPROVED PREMISES",
        probationDeliveryUnit = PDU
    )
    val NON_APPROVED_PREMISES_LAU = LocalAdminUnit(
        id = IdGenerator.getAndIncrement(),
        description = "Something else",
        probationDeliveryUnit = PDU
    )
}
