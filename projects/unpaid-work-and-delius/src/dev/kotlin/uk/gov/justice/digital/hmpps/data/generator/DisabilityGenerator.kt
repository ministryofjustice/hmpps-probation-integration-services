package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.DisabilityEntity
import java.time.LocalDate

object DisabilityGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = DisabilityEntity(
        id,
        CaseGenerator.DEFAULT,
        ReferenceDataGenerator.DISABILITY_HEARING,
        ReferenceDataGenerator.DISABILITY_HEARING_CONDITION,
        "wears hearing aid",
        LocalDate.now().minusMonths(5),
        null
    )
}
