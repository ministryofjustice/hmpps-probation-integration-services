package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.integrations.oasys.model.OasysHealthDetails
import java.time.ZonedDateTime

data class HealthDetails(
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
    val health: Health,
) : Assessment() {
    companion object {
        fun from(oasysHealthDetails: OasysHealthDetails): HealthDetails {
            with(oasysHealthDetails.assessments[0]) {
                return HealthDetails(
                    assessmentPk,
                    assessmentType,
                    dateCompleted,
                    assessorSignedDate,
                    initiationDate,
                    assessmentStatus,
                    superStatus,
                    laterWIPAssessmentExists,
                    oasysHealthDetails.limitedAccessOffender,
                    lastUpdatedDate,
                    Health(
                        stringToBoolean(generalHealth),
                        generalHeathSpecify,
                        elecMonSpecify,
                        stringToBoolean(elecMonElectricity),
                        stringToBoolean(elecMon),
                        generalHeathSpecify,
                        HealthDetail.from(healthCommunity, healthEM, healthProgramme),
                        HealthDetail.from(drugsCommunity, drugsEM, drugsProgramme),
                        HealthDetail.from(lifestyleCommunity, lifestyleEM, lifestyleProgramme),
                        HealthDetail.from(religiousCommunity, religiousEM, religiousProgramme),
                        HealthDetail.from(transportCommunity, transportEM, transportProgramme),
                        HealthDetail.from(employmentCommunity, employmentEM, employmentProgramme),
                        HealthDetail.from(educationCommunity, educationEM, educationProgramme),
                        HealthDetail.from(childCareCommunity, childCareEM, childCareProgramme),
                        HealthDetail.from(disabilityCommunity, disabilityEM, disabilityProgramme),
                        HealthDetail.from(psychiatricCommunity, psychiatricEM, psychiatricCommunity),
                        HealthDetail.from(motivationCommunity, motivationEM, motivationProgramme),
                        HealthDetail.from(learningCommunity, learningEM, learningProgramme),
                        HealthDetail.from(literacyCommunity, literacyEM, literacyProgramme),
                        HealthDetail.from(communicationCommunity, communicationEM, communicationProgramme),
                        HealthDetail.from(interpreterCommunity, interpreterEM, interpreterProgramme),
                        HealthDetail.from(alcoholCommunity, alcoholEM, alcoholProgramme),
                    ),
                )
            }
        }
    }
}

data class Health(
    val generalHealth: Boolean? = null,
    val generalHealthSpecify: String? = null,
    val electronicMonitoringSpecify: String? = null,
    val electronicMonitoringElectricity: Boolean? = null,
    val electronicMonitoring: Boolean? = null,
    val generalHeathSpecify: String? = null,
    val healthIssues: HealthDetail? = null,
    val drugsMisuse: HealthDetail? = null,
    val chaoticLifestyle: HealthDetail? = null,
    val religiousOrCulturalRequirements: HealthDetail? = null,
    val transportDifficulties: HealthDetail? = null,
    val employmentCommitments: HealthDetail? = null,
    val educationCommitments: HealthDetail? = null,
    val childCareAndCarers: HealthDetail? = null,
    val disability: HealthDetail? = null,
    val psychiatricPsychologicalRequirements: HealthDetail? = null,
    val levelOfMotivation: HealthDetail? = null,
    val learningDifficulties: HealthDetail? = null,
    val literacyProblems: HealthDetail? = null,
    val poorCommunicationSkills: HealthDetail? = null,
    val needForInterpreter: HealthDetail? = null,
    val alcoholMisuse: HealthDetail? = null,
)

data class HealthDetail(
    val community: String? = null,
    val electronicMonitoring: String? = null,
    val programme: String? = null,
) {
    companion object {
        fun from(
            community: String?,
            electronicMonitoring: String?,
            programme: String?,
        ): HealthDetail? {
            return if (community.isNullOrBlank() && electronicMonitoring.isNullOrBlank() && programme.isNullOrBlank()) {
                null
            } else {
                HealthDetail(community, electronicMonitoring, programme)
            }
        }
    }
}
