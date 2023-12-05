package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.notification.nomsId
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person

object PersonGenerator {
    val RELEASABLE = generate(NotificationGenerator.PRISONER_RELEASED.nomsId())
    val RECALLABLE = generate(NotificationGenerator.PRISONER_RECEIVED.nomsId())
    val DIED = generate(NotificationGenerator.PRISONER_DIED.nomsId())
    val MATCHABLE = generate(NotificationGenerator.PRISONER_MATCHED.nomsId())
    val NEW_CUSTODY = generate(NotificationGenerator.PRISONER_NEW_CUSTODY.nomsId())
    val RECALLED = generate(NotificationGenerator.PRISONER_RECALLED.nomsId())
    val HOSPITAL_RELEASED = generate(NotificationGenerator.PRISONER_HOSPITAL_RELEASED.nomsId())
    val HOSPITAL_IN_CUSTODY = generate(NotificationGenerator.PRISONER_HOSPITAL_IN_CUSTODY.nomsId())
    val ROTL = generate(NotificationGenerator.PRISONER_ROTL_RETURN.nomsId())
    val IRC_RELEASED = generate(NotificationGenerator.PRISONER_IRC_RELEASED.nomsId())
    val IRC_IN_CUSTODY = generate(NotificationGenerator.PRISONER_IRC_IN_CUSTODY.nomsId())
    val RELEASABLE_ECSL_ACTIVE = generate(NotificationGenerator.PRISONER_RELEASED_ECSL_ACTIVE.nomsId())
    val RELEASABLE_ECSL_INACTIVE = generate(NotificationGenerator.PRISONER_RELEASED_ECSL_INACTIVE.nomsId())
    val ABSCONDED = generate(NotificationGenerator.PRISONER_ABSCONDED.nomsId())
    val ETR_IN_CUSTODY = generate(NotificationGenerator.PRISONER_ETR_IN_CUSTODY.nomsId())

    fun generate(nomsNumber: String, id: Long = IdGenerator.getAndIncrement()) = Person(id, nomsNumber)
}
