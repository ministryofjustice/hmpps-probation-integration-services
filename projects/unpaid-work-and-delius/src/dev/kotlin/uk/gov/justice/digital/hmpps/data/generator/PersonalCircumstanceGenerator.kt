package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CaseEntity
import uk.gov.justice.digital.hmpps.controller.casedetails.entity.CasePersonalCircumstanceEntity
import java.time.LocalDate

object PersonalCircumstanceGenerator {
    val DEFAULT = generate()

    fun generate(
        person: CaseEntity = CaseGenerator.DEFAULT,
        id: Long = IdGenerator.getAndIncrement()
    ) = CasePersonalCircumstanceEntity(
        id,
        person,
        PersonalCircumstanceTypeGenerator.DEFAULT,
        PersonalCircumstanceSubTypeGenerator.DEFAULT,
        "Some notes",
        LocalDate.now(),
        null
    )
}
