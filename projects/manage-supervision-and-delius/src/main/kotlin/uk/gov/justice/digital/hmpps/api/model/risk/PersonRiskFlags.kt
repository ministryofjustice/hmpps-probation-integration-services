package uk.gov.justice.digital.hmpps.api.model.risk

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class PersonRiskFlags(
    val personSummary: PersonSummary,
    val riskFlags: List<RiskFlag>
)