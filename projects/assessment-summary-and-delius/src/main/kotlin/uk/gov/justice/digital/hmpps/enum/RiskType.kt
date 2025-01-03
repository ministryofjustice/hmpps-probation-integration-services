package uk.gov.justice.digital.hmpps.enum

import uk.gov.justice.digital.hmpps.integrations.oasys.AssessmentSummary

enum class RiskType(val code: String, val riskLevel: (AssessmentSummary) -> RiskLevel?) {
    CHILDREN("RCHD", { RiskLevel.of(it.riskChildrenCommunity, it.riskChildrenCustody) }),
    KNOWN_ADULT("REG15", { RiskLevel.of(it.riskKnownAdultCommunity, it.riskKnownAdultCustody) }),
    PRISONER("REG16", { RiskLevel.of(it.riskPrisonersCustody) }),
    PUBLIC("REG17", { RiskLevel.of(it.riskPublicCommunity, it.riskPublicCustody) }),
    STAFF("AV2S", { RiskLevel.of(it.riskStaffCommunity, it.riskStaffCustody) });
}