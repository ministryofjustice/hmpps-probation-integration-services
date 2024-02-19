package uk.gov.justice.digital.hmpps.service

sealed interface ActionResult {
    val properties: Map<String, String>

    data class Success(val type: Type, override val properties: Map<String, String> = mapOf()) : ActionResult
    data class Failure(val exception: Exception, override val properties: Map<String, String> = mapOf()) : ActionResult
    data class Ignored(val reason: String, override val properties: Map<String, String> = mapOf()) : ActionResult

    enum class Type {
        StandardLicenceConditionAdded,
        AdditionalLicenceConditionAdded,
        BespokeLicenceConditionAdded,
        NoChangeToLicenceConditions
    }
}
