package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonManager
import java.time.ZonedDateTime

object PersonGenerator {
    val NEW_TO_PROBATION = generate("N123456")
    val CURRENTLY_MANAGED = generate("C123456")
    val PREVIOUSLY_MANAGED = generate("P123456")
    val NO_SENTENCE = generate("U123456")

    fun generate(crn: String, softDeleted: Boolean = false, id: Long = IdGenerator.getAndIncrement()) =
        Person(crn, softDeleted, id)

    fun generatePersonManager(person: Person) =
        PersonManager(
            IdGenerator.getAndIncrement(),
            person,
            TeamGenerator.DEFAULT,
            StaffGenerator.ALLOCATED,
            ProviderGenerator.DEFAULT,
            ZonedDateTime.now()
        )
}
