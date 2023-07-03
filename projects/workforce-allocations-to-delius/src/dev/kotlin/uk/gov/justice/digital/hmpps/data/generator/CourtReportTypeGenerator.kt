package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.CourtReportType

object CourtReportTypeGenerator {
    val DEFAULT = generate()

    fun generate(description: String = "Court Report Type", id: Long = IdGenerator.getAndIncrement()) =
        CourtReportType(id, description)
}
