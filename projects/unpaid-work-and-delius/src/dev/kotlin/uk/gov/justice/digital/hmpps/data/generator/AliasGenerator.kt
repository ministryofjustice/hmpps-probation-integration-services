package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.AliasEntity
import java.time.LocalDate

object AliasGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = AliasEntity(
        id,
        "Tony",
        null,
        null,
        "Stark",
        LocalDate.now().minusYears(18),
        CaseGenerator.DEFAULT
    )
}
