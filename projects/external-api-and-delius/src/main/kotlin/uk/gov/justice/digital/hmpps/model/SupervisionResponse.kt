package uk.gov.justice.digital.hmpps.model

data class SupervisionResponse(
    val mappaDetail: MappaDetail?,
    val supervisions: List<Supervision>
)
