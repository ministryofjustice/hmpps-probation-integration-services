package uk.gov.justice.digital.hmpps.integrations.oasys.model

import java.time.ZonedDateTime

abstract class OasysAssessment {
    abstract val assessmentPk: Long
    abstract val assessmentType: String
    abstract val dateCompleted: ZonedDateTime?
    abstract val assessorSignedDate: ZonedDateTime?
    abstract val initiationDate: ZonedDateTime
    abstract val assessmentStatus: String
    abstract val superStatus: String?
    abstract val laterWIPAssessmentExists: Boolean?
}
