package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val RSR_SCORES_DETERMINED = ResourceLoader.message<HmppsDomainEvent>("rsr-scores-determined")
    val OGRS_SCORES_DETERMINED = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined")
    val OGRS_SCORES_DETERMINED_UPDATE = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined-update")
}
