package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person

object PersonGenerator {

    val PERSON1: Person = generate("A123456", "Jon", "Harry", "Fred", "Smith")
    val PERSON2: Person = generate("A123456", "Jon", "Harry", surname =  "Smith")
    val PERSON3: Person = generate("A123456", "Jon", thirdName = "Fred", surname = "Smith")
    val PERSON4: Person = generate("A123456", "Jon", surname = "Smith")

    fun generate(
        crn: String,
        forename: String,
        secondName: String? = null,
        thirdName: String? = null,
        surname: String,
    ) = Person(IdGenerator.getAndIncrement(), crn, forename, secondName, thirdName, surname)
}