package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.casedetails.entity.MainOffence

object MainOffenceGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = MainOffence(id, EventGenerator.DEFAULT, OffenceGenerator.DEFAULT)
}
