package uk.gov.justice.digital.hmpps.integrations.oasys.model

import java.time.ZonedDateTime

data class OasysRiskAssessmentDetails(
    val limitedAccessOffender: Boolean,
    val assessments: List<OasysRiskAssessment>,
)

data class OasysRiskAssessment(
    override val assessmentPk: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val lastUpdatedDate: ZonedDateTime? = null,
    val currentOffenceDetails: String? = null,
    val currentSources: String? = null,
    val currentWhyDone: String? = null,
    val currentAnyoneElsePresent: String? = null,
    val currentWhoVictims: String? = null,
    val currentHowDone: String? = null,
    val currentWhereAndWhen: String? = null,
    val previousSources: String? = null,
    val previousWhyDone: String? = null,
    val previousAnyoneElsePresent: String? = null,
    val previousWhoVictims: String? = null,
    val previousHowDone: String? = null,
    val previousWhereAndWhen: String? = null,
    val previousWhatDone: String? = null,
) : OasysAssessment()
