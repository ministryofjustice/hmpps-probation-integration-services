package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object PersonGenerator {
    val DEFAULT = Person(IdGenerator.getAndIncrement(), "A5089DY")
    val PERSON_WITH_KEYDATES = Person(IdGenerator.getAndIncrement(), "A0001DY")
}
