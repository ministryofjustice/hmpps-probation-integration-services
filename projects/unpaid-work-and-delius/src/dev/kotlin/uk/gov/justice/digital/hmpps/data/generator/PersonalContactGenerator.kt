package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.personaldetails.entity.PersonalContactEntity
import java.time.LocalDate

object PersonalContactGenerator {
    val DEFAULT = generate()

    fun generate(id: Long = IdGenerator.getAndIncrement()) = PersonalContactEntity(
        id,
        PersonGenerator.DEFAULT,
        "Captains mate",
        "Shiver",
        "Me",
        "Timbers",
        "0779999887",
        AddressGenerator.DEFAULT,
        LocalDate.now()
    )
}
