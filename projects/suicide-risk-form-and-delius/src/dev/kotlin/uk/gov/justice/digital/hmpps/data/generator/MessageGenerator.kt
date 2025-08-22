package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val CREATED = ResourceLoader.message<HmppsDomainEvent>("suicide-risk-form-created")
    val DELETED = ResourceLoader.message<HmppsDomainEvent>("suicide-risk-form-deleted")
}
