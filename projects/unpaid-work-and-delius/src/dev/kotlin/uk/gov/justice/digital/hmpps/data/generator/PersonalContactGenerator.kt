package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CasePersonalContactEntity
import java.time.LocalDate

object PersonalContactGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = CasePersonalContactEntity(
        id,
        CaseGenerator.DEFAULT,
        "Captains mate",
        ReferenceDataGenerator.DOCTOR_RELATIONSHIP,
        "Shiver",
        "Me",
        "Timbers",
        "0779999887",
        AddressGenerator.DEFAULT,
        LocalDate.now()
    )
}
