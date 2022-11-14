package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime

abstract class Assessment {
    abstract val assessmentId: Long
    abstract val assessmentType: String
    abstract val dateCompleted: ZonedDateTime?
    abstract val assessorSignedDate: ZonedDateTime?
    abstract val initiationDate: ZonedDateTime
    abstract val assessmentStatus: String
    abstract val superStatus: String?
    abstract val laterWIPAssessmentExists: Boolean?
    abstract val limitedAccessOffender: Boolean
}
