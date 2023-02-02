package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.RegistrationEntity
import java.time.LocalDate

object RegistrationGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = RegistrationEntity(
        id,
        LocalDate.now().minusMonths(3),
        CaseGenerator.DEFAULT,
        ReferenceDataGenerator.MAPPA_CATEGORY_2,
        RegisterTypeGenerator.DEFAULT,
        ReferenceDataGenerator.MAPPA_LEVEL_1
    )
}
