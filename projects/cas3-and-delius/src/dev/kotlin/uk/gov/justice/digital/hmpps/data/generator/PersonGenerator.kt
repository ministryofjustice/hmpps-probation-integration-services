package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager

object PersonGenerator {
    val PERSON_CRN = generate("A000001", null)
    val PERSON_2_CRN = generate("A000002", null)

    fun generate(crn: String, noms: String?, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        Person(crn, noms, softDeleted, id)

    fun generatePersonManager(person: Person) =
        PersonManager(IdGenerator.getAndIncrement(), person.id, 1, 1, 1)
}
