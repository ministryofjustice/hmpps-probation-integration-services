package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.staff.LocalAdminUnit
import uk.gov.justice.digital.hmpps.entity.staff.ProbationDeliveryUnit

object LauGenerator {
    val DEFAULT_PROVIDER_LOCAL_ADMIN_UNIT =
        generateLocalAdminUnit(code = "N01LAU", probationDeliveryUnit = PduGenerator.DEFAULT_PROVIDER_PDU)

    val SECOND_PROVIDER_LOCAL_ADMIN_UNIT =
        generateLocalAdminUnit(code = "N02LAU", probationDeliveryUnit = PduGenerator.SECOND_PROVIDER_PDU)

    fun generateLocalAdminUnit(
        id: Long = IdGenerator.getAndIncrement(),
        code: String,
        probationDeliveryUnit: ProbationDeliveryUnit,
        selectable: Boolean = true,
    ) = LocalAdminUnit(
        id = id,
        code = code,
        probationDeliveryUnit = probationDeliveryUnit,
        selectable = selectable,
    )
}

