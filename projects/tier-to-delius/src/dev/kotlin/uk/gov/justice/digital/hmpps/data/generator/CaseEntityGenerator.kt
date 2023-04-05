package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.controller.entity.CaseEntity
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object CaseEntityGenerator {

    val DEFAULT = generate("D001022")

    fun generate(
        crn: String,
        id: Long = IdGenerator.getAndIncrement()
    ): CaseEntity {
        return CaseEntity(id = id, crn = crn, gender = , dynamicRsrScore = 10.1, tier =  )
    }
}