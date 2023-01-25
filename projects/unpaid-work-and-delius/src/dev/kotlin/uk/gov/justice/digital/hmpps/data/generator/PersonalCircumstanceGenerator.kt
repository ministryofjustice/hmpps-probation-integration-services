package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.Person
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalCircumstanceEntity
import java.time.LocalDate

object PersonalCircumstanceGenerator {
    val DEFAULT = generate()

    fun generate(person: Person = PersonGenerator.DEFAULT, id: Long = IdGenerator.getAndIncrement()) = PersonalCircumstanceEntity(
        id,
        person,
        PersonalCircumstanceTypeGenerator.DEFAULT,
        PersonalCircumstanceSubTypeGenerator.DEFAULT,
        "Some notes",
        LocalDate.now(),
        null
    )
}
