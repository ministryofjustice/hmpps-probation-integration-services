package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

object PersonGenerator {
    val DEFAULT = Person("T123456", IdGenerator.getAndIncrement())
}