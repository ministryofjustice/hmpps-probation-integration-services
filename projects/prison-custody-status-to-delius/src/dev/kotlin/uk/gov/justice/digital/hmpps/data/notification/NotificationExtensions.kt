package uk.gov.justice.digital.hmpps.data.notification

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification

fun Notification<HmppsDomainEvent>.nomsId() = message.personReference.findNomsNumber()!!
