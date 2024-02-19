package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.document.entity.InstitutionalReport
import java.time.LocalDate

object InstitutionalReportGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = InstitutionalReport(
        id,
        ReferenceDataGenerator.INS_RPT_PAR,
        LocalDate.now().plusWeeks(1),
        custodyId = CustodyGenerator.DEFAULT.id
    )
}
