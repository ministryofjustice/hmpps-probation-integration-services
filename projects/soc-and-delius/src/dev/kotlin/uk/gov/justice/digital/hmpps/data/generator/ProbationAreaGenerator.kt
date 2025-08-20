package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Borough
import uk.gov.justice.digital.hmpps.entity.District
import uk.gov.justice.digital.hmpps.entity.ProbationAreaEntity

object ProbationAreaGenerator {
    val DEFAULT_PA = ProbationAreaEntity(
        true,
        "NPS North East",
        "N02",
        null,
        IdGenerator.getAndIncrement()
    )
    val DEFAULT_BOROUGH = Borough(
        true,
        IdGenerator.getAndIncrement(),
        DEFAULT_PA,
        "N02",
        "Borough N02"
    )
    val DEFAULT_LDU = District(
        true,
        "D01",
        "Durham",
        DEFAULT_BOROUGH,
        IdGenerator.getAndIncrement()
    )
    val DEFAULT_LDU2 = District(
        true,
        "D02",
        "Durham2",
        DEFAULT_BOROUGH,
        IdGenerator.getAndIncrement()
    )
    val NON_SELECTABLE_PA = ProbationAreaEntity(
        false,
        "NPS North West",
        "N03",
        null,
        IdGenerator.getAndIncrement()
    )
    val NON_SELECTABLE_BOROUGH = Borough(
        true,
        IdGenerator.getAndIncrement(),
        NON_SELECTABLE_PA,
        "N03",
        "Borough N03"
    )
    val NON_SELECTABLE_LDU = District(
        true,
        "D02",
        "Manchester",
        NON_SELECTABLE_BOROUGH,
        IdGenerator.getAndIncrement()
    )

    fun generateProbationArea(
        code: String,
        description: String = "Area of $code",
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ProbationAreaEntity(selectable, description, code, null, id)

    fun generatePdu(
        code: String,
        description: String = "Delivery Unit of $code",
        area: ProbationAreaEntity,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = Borough(selectable, id, area, code, description)

    fun generateLau(
        code: String,
        description: String = "Admin Unit of $code",
        pdu: Borough,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = District(selectable, code, description, pdu, id)
}
