package uk.gov.justice.digital.hmpps

import uk.gov.justice.digital.hmpps.message.HmppsEvent
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

object MessageGenerator {
    val RSR_SCORES_DETERMINED = ResourceLoader.message<HmppsEvent>("rsr-scores-determined")
    val OGRS_SCORES_DETERMINED = ResourceLoader.message<HmppsEvent>("ogrs-scores-determined")
}
