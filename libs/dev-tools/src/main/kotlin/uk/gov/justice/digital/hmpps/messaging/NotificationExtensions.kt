package uk.gov.justice.digital.hmpps.messaging

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference

object NotificationExtensions {
    fun Notification<HmppsDomainEvent>.withCrn(crn: String) = copy(
        message = message.copy(personReference = PersonReference(listOf(PersonIdentifier("CRN", crn))))
    )
}