package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person

object PersonGenerator {

    val DEFAULT: Person = generate("A123456", "Jon", "Harry", "Fred", "Smith")
    fun generate(
        crn: String,
        forename: String,
        secondName: String? = null,
        thirdName: String? = null,
        surname: String,
    ) = Person(IdGenerator.getAndIncrement(), crn, forename, secondName, thirdName, surname)
}