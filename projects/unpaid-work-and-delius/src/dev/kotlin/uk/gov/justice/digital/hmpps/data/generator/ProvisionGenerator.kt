package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.ProvisionEntity
import java.time.LocalDate

object ProvisionGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = ProvisionEntity(
        id,
        CaseGenerator.DEFAULT,
        ReferenceDataGenerator.HEARING_PROVISION,
        LocalDate.now().minusMonths(5)
    )
}
