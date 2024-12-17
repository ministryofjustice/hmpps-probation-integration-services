package uk.gov.justice.digital.hmpps.api.model.risk

import uk.gov.justice.digital.hmpps.api.model.PersonSummary

data class PersonRiskFlags(
    val personSummary: PersonSummary,
    val opd: Opd? = null,
    val mappa: MappaDetail? = null,
    val riskFlags: List<RiskFlag>,
    val removedRiskFlags: List<RiskFlag> = emptyList(),
)