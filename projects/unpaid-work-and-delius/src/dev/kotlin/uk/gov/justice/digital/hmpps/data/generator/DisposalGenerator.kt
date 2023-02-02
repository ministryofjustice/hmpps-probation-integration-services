package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.Disposal
import java.time.LocalDate

object DisposalGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(id, EventGenerator.DEFAULT, LocalDate.now().minusMonths(5))
}
