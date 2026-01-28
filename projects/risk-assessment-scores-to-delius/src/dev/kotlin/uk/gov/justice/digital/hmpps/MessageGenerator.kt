package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val RSR_SCORES_DETERMINED = ResourceLoader.message<HmppsDomainEvent>("rsr-scores-determined")
    val RSR_SCORES_DETERMINED_V4 = ResourceLoader.message<HmppsDomainEvent>("rsr-scores-determined-v4")
    val RSR_SCORES_DETERMINED_WITHOUT_OSPIIC_OSPDC =
        ResourceLoader.message<HmppsDomainEvent>("rsr-scores-determined-null-osp")
    val RSR_SCORES_DETERMINED_WITH_OSPII_OSPDC =
        ResourceLoader.message<HmppsDomainEvent>("rsr-scores-determined-osp-ii-dc")
    val OGRS_SCORES_DETERMINED = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined")
    val OGRS_SCORES_DETERMINED_UPDATE = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined-update")
    val OGRS_SCORES_NULL_EVENT = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined-null-event")
    val OGRS_SCORES_MERGED = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined-merged")
    val OGRS_SCORES_DETERMINED_OGRS4 = ResourceLoader.message<HmppsDomainEvent>("ogrs-scores-determined-ogrs4")
}
