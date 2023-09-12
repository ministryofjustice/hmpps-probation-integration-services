package uk.gov.justice.digital.hmpps.integrations.opd

import java.time.ZonedDateTime

data class OPDAssessment(
    val assessmentDate: ZonedDateTime,
    val opdScore: String
)

fun OPDAssessment.telemetryProperties(crn: String) = mapOf(
    "crn" to crn,
    "assessmentDate" to assessmentDate.toString(),
    "opdScode" to opdScore
)
