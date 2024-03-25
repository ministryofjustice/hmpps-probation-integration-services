package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.ProvisionEntity
import java.time.LocalDate

object ProvisionGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = ProvisionEntity(
        id,
        CaseGenerator.DEFAULT,
        ReferenceDataGenerator.HEARING_PROVISION,
        ReferenceDataGenerator.HEARING_PROVISION_CATEGORY,
        "Knock loudly on the door",
        LocalDate.now().minusMonths(5)
    )
}
