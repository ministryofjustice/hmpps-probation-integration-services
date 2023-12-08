package uk.gov.justice.digital.hmpps.messaging

import org.springframework.boot.context.properties.ConfigurationProperties
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody

@ConfigurationProperties(prefix = "prisoner.movement")
data class PrisonerMovementConfigs(
    val configs: List<PrisonerMovementConfig>,
)

data class PrisonerMovementConfig(
    val types: List<PrisonerMovement.Type>,
    val reasons: List<String> = listOf(),
    val actionNames: List<String> = listOf(),
    val featureFlag: String? = null,
    val reasonOverride: String? = null,
) {
    fun validFor(
        type: PrisonerMovement.Type,
        reason: String,
    ): Boolean =
        type in types && (reasons.isEmpty() || reason in reasons)
}

interface PrisonerMovementAction {
    val name: String

    fun accept(context: PrisonerMovementContext): ActionResult
}

data class PrisonerMovementContext(
    val prisonerMovement: PrisonerMovement,
    val custody: Custody,
)

sealed interface ActionResult {
    val properties: Map<String, String>

    data class Success(val type: Type, override val properties: Map<String, String> = mapOf()) : ActionResult

    data class Failure(val exception: Exception, override val properties: Map<String, String> = mapOf()) : ActionResult

    data class Ignored(val reason: String, override val properties: Map<String, String> = mapOf()) : ActionResult

    enum class Type {
        Died,
        LocationUpdated,
        Recalled,
        Released,
        StatusUpdated,
    }
}
