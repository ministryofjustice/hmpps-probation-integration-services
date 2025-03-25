package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.service.CaseAccess

data class LimitedAccessDetail(val crn: String, val hasExclusion: Boolean, val hasRestriction: Boolean) {
    val isLimitedAccess: Boolean get() = hasExclusion || hasRestriction
}

fun CaseAccess.limitedAccessDetail() = LimitedAccessDetail(crn, userExcluded, userRestricted)