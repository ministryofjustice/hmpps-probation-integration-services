package uk.gov.justice.digital.hmpps.api.model.risk

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class PersonRiskFlag (
    val personSummary: PersonSummary,
    val riskFlag: RiskFlag
)