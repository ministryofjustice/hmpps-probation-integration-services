package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.integration.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integration.delius.entity.ProbationArea

object ProbationAreaGenerator {
    val DO_NOT_FIND_PA = generateProbationArea("XXX")
    val DEFAULT = generateProbationArea("N01")

    fun generateProbationArea(code: String, id: Long = IdGenerator.getAndIncrement()) = ProbationArea(code, id)
    fun generatePersonManager(
        person: Person,
        probationArea: ProbationArea = DEFAULT,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(person, probationArea, active, softDeleted, id)
}