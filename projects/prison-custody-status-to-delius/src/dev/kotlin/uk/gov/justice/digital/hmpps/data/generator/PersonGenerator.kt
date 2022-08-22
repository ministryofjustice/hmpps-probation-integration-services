package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.listener.nomsNumber

object PersonGenerator {
    val RELEASABLE = generate(MessageGenerator.PRISONER_RELEASED.additionalInformation.nomsNumber())

    fun generate(nomsNumber: String, id: Long = IdGenerator.getAndIncrement()) = Person(id, nomsNumber)
}
