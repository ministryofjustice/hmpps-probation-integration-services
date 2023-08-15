package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

object PersonGenerator {
    val RELEASABLE = generate(NotificationGenerator.PRISONER_RELEASED.message.personReference.findNomsNumber()!!)
    val RECALLABLE = generate(NotificationGenerator.PRISONER_RECEIVED.message.personReference.findNomsNumber()!!)
    val DIED = generate(NotificationGenerator.PRISONER_DIED.message.personReference.findNomsNumber()!!)
    val MATCHABLE = generate(NotificationGenerator.PRISONER_MATCHED.message.personReference.findNomsNumber()!!)
    val NEW_CUSTODY = generate(NotificationGenerator.PRISONER_NEW_CUSTODY.message.personReference.findNomsNumber()!!)
    val RECALLED = generate(NotificationGenerator.PRISONER_RECALLED.message.personReference.findNomsNumber()!!)
    val HOSPITAL_RELEASED = generate(NotificationGenerator.PRISONER_HOSPITAL_RELEASED.message.personReference.findNomsNumber()!!)
    val HOSPITAL_IN_CUSTODY = generate(NotificationGenerator.PRISONER_HOSPITAL_IN_CUSTODY.message.personReference.findNomsNumber()!!)
    val ROTL = generate(NotificationGenerator.PRISONER_ROTL_RETURN.message.personReference.findNomsNumber()!!)

    fun generate(nomsNumber: String, id: Long = IdGenerator.getAndIncrement()) = Person(id, nomsNumber)
}
