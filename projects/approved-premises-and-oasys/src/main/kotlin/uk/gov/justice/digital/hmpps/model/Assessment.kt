package uk.gov.justice.digital.hmpps.model

import java.time.ZonedDateTime
import java.util.Locale

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

    companion object {
        fun stringToBoolean(string: String?): Boolean? {
            when (string?.uppercase(Locale.getDefault())) {
                "[YES]" -> return true
                "[NO]" -> return false
            }
            return null
        }
    }
}
