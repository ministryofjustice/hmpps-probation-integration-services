package uk.gov.justice.digital.hmpps.integrations.delius.model

import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Success

sealed interface MergeResult {

    data class Success(val crn: String, val noms: String, val action: Action) : MergeResult
    data class Failure(val exception: Exception) : MergeResult

    enum class Action {
        Created, Updated
    }
}

fun Success?.properties(): Map<String, String> =
    if (this == null) emptyMap() else listOfNotNull("crn" to crn, "noms" to noms, "action" to action.name).toMap()