package uk.gov.justice.digital.hmpps.api.model

data class ProbationRecord(
    val crn: String,
    val nomsId: String,
    val currentTier: String?,
    val resourcing: Resourcing?,
    val manager: Manager,
    val mappaLevel: Int
)

enum class Resourcing {
    ENHANCED, NORMAL
}

data class Manager(val code: String, val name: Name, val team: Team)
data class Name(val forename: String, val middleName: String?, val surname: String)
data class Team(val code: String, val description: String, val localDeliveryUnit: LocalDeliveryUnit?)
data class LocalDeliveryUnit(val code: String, val description: String)
