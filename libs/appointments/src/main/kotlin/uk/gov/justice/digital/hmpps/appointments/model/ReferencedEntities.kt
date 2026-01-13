package uk.gov.justice.digital.hmpps.appointments.model

data class ReferencedEntities(
    val crn: String? = null,
    val personId: Long? = null,
    val eventId: Long? = null,
    val nonStatutoryInterventionId: Long? = null,
    val licenceConditionId: Long? = null,
    val requirementId: Long? = null,
    val pssRequirementId: Long? = null,
) {
    init {
        require(personId != null || crn != null) { "Either personId or crn must be provided" }
        require((requirementId == null && licenceConditionId == null && pssRequirementId == null) || eventId != null) {
            "eventId must be provided when requirementId, licenceConditionId or pssRequirementId is set"
        }
    }
}