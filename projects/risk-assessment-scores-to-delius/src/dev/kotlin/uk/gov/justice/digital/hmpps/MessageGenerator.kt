package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val RSR_SCORES_DETERMINED = ResourceLoader.message<HmppsDomainEvent>("rsr-scores-determined")
    val RSR_SCORES_DETERMINED_WITHOUT_OSPIIC_OSPDC =
        ResourceLoader.message<HmppsDomainEvent>("rsr-scores-determined-null-osp")
    val RSR_SCORES_DETERMINED_WITH_OSPII_OSPDC =
        ResourceLoader.message<HmppsDomainEvent>("rsr-scores-determined-osp-ii-dc")
    val OGRS_SCORES_DETERMINED = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined")
    val OGRS_SCORES_DETERMINED_UPDATE = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined-update")
}
