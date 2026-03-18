package uk.gov.justice.digital.hmpps.model

data class RequirementsResponse(
    val requirements: List<CossoRequirement>,
    val breachReasons: List<CodeAndDescription>
)

data class CossoRequirement(
    val id: Long,
    val type: CodeAndDescription,
    val subType: CodeAndDescription,
)


