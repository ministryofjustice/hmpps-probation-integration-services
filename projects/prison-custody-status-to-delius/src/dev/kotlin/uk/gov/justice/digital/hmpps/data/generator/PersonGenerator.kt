package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.messaging.nomsNumber

object PersonGenerator {
    val RELEASABLE = generate(MessageGenerator.PRISONER_RELEASED.additionalInformation.nomsNumber())
    val RECALLABLE = generate(MessageGenerator.PRISONER_RECEIVED.additionalInformation.nomsNumber())
    val DIED = generate(MessageGenerator.PRISONER_DIED.additionalInformation.nomsNumber())
    val MOVEABLE = generate("A5295DZ")

    fun generate(nomsNumber: String, id: Long = IdGenerator.getAndIncrement()) = Person(id, nomsNumber)
}
