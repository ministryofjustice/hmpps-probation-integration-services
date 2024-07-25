package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.Person

object PersonGenerator {
    val DEFAULT = Person("T123456", IdGenerator.getAndIncrement())
}