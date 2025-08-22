package uk.gov.justice.digital.hmpps.integrations.delius.model

import uk.gov.justice.digital.hmpps.integrations.delius.model.MergeResult.Success

sealed interface MergeResult {

    data class Success(val crn: String, val noms: String, val action: Action) : MergeResult
    data class Failure(val exception: Exception) : MergeResult

    sealed interface Action {
        val name: String

        data object Created : Action {
            override val name = "Created"
        }

        data object Updated : Action {
            override val name = "Updated"
        }

        data class Moved(val from: String, val to: String) : Action {
            override val name = "Moved"
        }
    }
}

fun Success?.properties(): Map<String, String> =
    if (this == null) emptyMap() else listOfNotNull("crn" to crn, "noms" to noms, "action" to action.name).toMap()