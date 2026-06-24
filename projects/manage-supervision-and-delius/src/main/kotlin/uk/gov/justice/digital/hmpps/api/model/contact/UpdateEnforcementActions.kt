package uk.gov.justice.digital.hmpps.api.model.contact

data class UpdateEnforcementActions(
    val enforcementActions: List<EnforcementActionForUpdate>
)

data class EnforcementActionForUpdate(
    val code: String,
)
