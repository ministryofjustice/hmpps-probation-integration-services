package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.LocalAdminUnit
import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnit

object LocalAdminUnitGenerator {
    val DEFAULT = generate(code = "LAU001", description = "Central LAU", pdu = ProbationDeliveryUnitGenerator.DEFAULT)
    val SECOND = generate(code = "LAU002", description = "East LAU", pdu = ProbationDeliveryUnitGenerator.SECOND)
    val THIRD = generate(code = "LAU003", description = "North LAU", pdu = ProbationDeliveryUnitGenerator.THIRD)
    val LONDON = generate(code = "LAU_LON", description = "London LAU", pdu = ProbationDeliveryUnitGenerator.LONDON_PDU)
    val INACTIVE = generate(code = "LAU050", description = "Inactive LAU", pdu = ProbationDeliveryUnitGenerator.DEFAULT, selectable = false)

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        description: String,
        pdu: ProbationDeliveryUnit,
        selectable: Boolean = true
    ) = LocalAdminUnit(id, code, description, pdu, selectable)
}

