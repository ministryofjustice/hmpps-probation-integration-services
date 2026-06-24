package uk.gov.justice.digital.hmpps.api.model.contact

import uk.gov.justice.digital.hmpps.api.model.compliance.EnforcementAction

data class UpdateEnforcementActions(
    val enforcementActions: List<EnforcementAction>
)
