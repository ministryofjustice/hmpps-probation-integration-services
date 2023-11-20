package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData

object ReferenceDataGenerator {
    val DISPOSAL_TYPE = ReferenceData(
        "D1",
        "Disposal type",
        IdGenerator.getAndIncrement()
    )
    val CUSTODIAL_STATUS = ReferenceData(
        "C1",
        "Custodial status",
        IdGenerator.getAndIncrement()
    )
    val LENGTH_UNITS = ReferenceData(
        "U1",
        "Days",
        IdGenerator.getAndIncrement()
    )
    val TERMINATION_REASON = ReferenceData(
        "R1",
        "Released",
        IdGenerator.getAndIncrement()
    )
}
