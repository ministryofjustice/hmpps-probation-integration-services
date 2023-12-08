package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysNeedsDetails
import java.time.ZonedDateTime

data class NeedsDetails(
    override val assessmentId: Long,
    override val assessmentType: String,
    override val dateCompleted: ZonedDateTime? = null,
    override val assessorSignedDate: ZonedDateTime? = null,
    override val initiationDate: ZonedDateTime,
    override val assessmentStatus: String,
    override val superStatus: String? = null,
    override val laterWIPAssessmentExists: Boolean? = null,
    override val limitedAccessOffender: Boolean,
    override val lastUpdatedDate: ZonedDateTime? = null,
    val needs: Needs? = null,
    val linksToHarm: LinksToHarm? = null,
    val linksToReOffending: LinksToReOffending? = null,
) : Assessment() {
    companion object {
        fun from(oasysNeeds: OasysNeedsDetails): NeedsDetails {
            with(oasysNeeds.assessments[0]) {
                return NeedsDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysNeeds.limitedAccessOffender,
                    lastUpdatedDate,
                    Needs(
                        offenceAnalysisDetails,
                        emoIssuesDetails,
                        drugIssuesDetails,
                        alcoholIssuesDetails,
                        lifestyleIssuesDetails,
                        relIssuesDetails,
                        financeIssuesDetails,
                        eTEIssuesDetails,
                        accIssuesDetails,
                        attIssuesDetails,
                        thingIssuesDetails,
                    ),
                    LinksToHarm.from(
                        LinksToHarm(
                            accommodationLinkedToHarm = stringToBoolean(accLinkedToHarm),
                            educationTrainingEmploymentLinkedToHarm = stringToBoolean(eTELinkedToHarm),
                            financeLinkedToHarm = stringToBoolean(financeLinkedToHarm),
                            relationshipLinkedToHarm = stringToBoolean(relLinkedToHarm),
                            lifestyleLinkedToHarm = stringToBoolean(lifestyleLinkedToHarm),
                            drugLinkedToHarm = stringToBoolean(drugLinkedToHarm),
                            alcoholLinkedToHarm = stringToBoolean(alcoholLinkedToHarm),
                            emotionalLinkedToHarm = stringToBoolean(emoLinkedToHarm),
                            thinkingBehaviouralLinkedToHarm = stringToBoolean(thinkLinkedToHarm),
                            attitudeLinkedToHarm = stringToBoolean(attLinkedToHarm),
                        ),
                    ),
                    LinksToReOffending.from(
                        LinksToReOffending(
                            accommodationLinkedToReOffending = stringToBoolean(accLinkedToReoffending),
                            educationTrainingEmploymentLinkedToReOffending = stringToBoolean(eTELinkedToReoffending),
                            financeLinkedToReOffending = stringToBoolean(financeLinkedToReoffending),
                            relationshipLinkedToReOffending = stringToBoolean(relLinkedToReoffending),
                            lifestyleLinkedToReOffending = stringToBoolean(lifestyleLinkedToReoffending),
                            drugLinkedToReOffending = stringToBoolean(drugLinkedToReoffending),
                            alcoholLinkedToReOffending = stringToBoolean(alcoholLinkedToReoffending),
                            emotionalLinkedToReOffending = stringToBoolean(emoLinkedToReoffending),
                            thinkingBehaviouralLinkedToReOffending = stringToBoolean(thinkLinkedToReoffending),
                            attitudeLinkedToReOffending = stringToBoolean(attLinkedToReoffending),
                        ),
                    ),
                )
            }
        }
    }
}

data class LinksToReOffending(
    val accommodationLinkedToReOffending: Boolean? = null,
    val educationTrainingEmploymentLinkedToReOffending: Boolean? = null,
    val financeLinkedToReOffending: Boolean? = null,
    val relationshipLinkedToReOffending: Boolean? = null,
    val lifestyleLinkedToReOffending: Boolean? = null,
    val drugLinkedToReOffending: Boolean? = null,
    val alcoholLinkedToReOffending: Boolean? = null,
    val emotionalLinkedToReOffending: Boolean? = null,
    val thinkingBehaviouralLinkedToReOffending: Boolean? = null,
    val attitudeLinkedToReOffending: Boolean? = null,
) {
    companion object {
        fun from(linksToReOffending: LinksToReOffending?): LinksToReOffending? {
            with(linksToReOffending) {
                return if (this?.accommodationLinkedToReOffending == null &&
                    this?.educationTrainingEmploymentLinkedToReOffending == null &&
                    this?.financeLinkedToReOffending == null &&
                    this?.relationshipLinkedToReOffending == null &&
                    this?.lifestyleLinkedToReOffending == null &&
                    this?.drugLinkedToReOffending == null &&
                    this?.alcoholLinkedToReOffending == null &&
                    this?.emotionalLinkedToReOffending == null &&
                    this?.thinkingBehaviouralLinkedToReOffending == null &&
                    this?.attitudeLinkedToReOffending == null
                ) {
                    null
                } else {
                    linksToReOffending
                }
            }
        }
    }
}

data class LinksToHarm(
    val accommodationLinkedToHarm: Boolean? = null,
    val educationTrainingEmploymentLinkedToHarm: Boolean? = null,
    val financeLinkedToHarm: Boolean? = null,
    val relationshipLinkedToHarm: Boolean? = null,
    val lifestyleLinkedToHarm: Boolean? = null,
    val drugLinkedToHarm: Boolean? = null,
    val alcoholLinkedToHarm: Boolean? = null,
    val emotionalLinkedToHarm: Boolean? = null,
    val thinkingBehaviouralLinkedToHarm: Boolean? = null,
    val attitudeLinkedToHarm: Boolean? = null,
) {
    companion object {
        fun from(linksToHarm: LinksToHarm?): LinksToHarm? {
            with(linksToHarm) {
                return if (this?.accommodationLinkedToHarm == null &&
                    this?.educationTrainingEmploymentLinkedToHarm == null &&
                    this?.financeLinkedToHarm == null &&
                    this?.relationshipLinkedToHarm == null &&
                    this?.lifestyleLinkedToHarm == null &&
                    this?.drugLinkedToHarm == null &&
                    this?.alcoholLinkedToHarm == null &&
                    this?.emotionalLinkedToHarm == null &&
                    this?.thinkingBehaviouralLinkedToHarm == null &&
                    this?.attitudeLinkedToHarm == null
                ) {
                    null
                } else {
                    linksToHarm
                }
            }
        }
    }
}

data class Needs(
    val offenceAnalysisDetails: String? = null,
    val emotionalIssuesDetails: String? = null,
    val drugIssuesDetails: String? = null,
    val alcoholIssuesDetails: String? = null,
    val lifestyleIssuesDetails: String? = null,
    val relationshipIssuesDetails: String? = null,
    val financeIssuesDetails: String? = null,
    val educationTrainingEmploymentIssuesDetails: String? = null,
    val accommodationIssuesDetails: String? = null,
    val attitudeIssuesDetails: String? = null,
    val thinkingBehaviouralIssuesDetails: String? = null,
)
