package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.Person

object PersonGenerator {
    val DEFAULT_PERSON = generatePerson("A000001")
    fun generatePerson(
        crn: String,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(id, crn)
}