package uk.gov.justice.digital.hmpps.model

data class ProbationArea(
    val code: String,
    val description: String,
    val localDeliveryUnits: List<LocalDeliveryUnit>,
)

data class LocalDeliveryUnit(
    val code: String,
    val description: String,
)

data class ProbationAreaContainer(
    val probationAreas: List<ProbationArea>,
)
