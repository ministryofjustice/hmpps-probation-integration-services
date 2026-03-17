package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val RECEIVED_A000001 = ResourceLoader.get<HmppsDomainEvent>("esupervision-received-A000001")
    val RECEIVED_A000004 = ResourceLoader.get<HmppsDomainEvent>("esupervision-received-A000004")
    val EXPIRED_A000001 = ResourceLoader.get<HmppsDomainEvent>("esupervision-expired-A000001")
}
