package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

object PersonGenerator {
    val DEFAULT = Person(IdGenerator.getAndIncrement(), "A5089DY")
}