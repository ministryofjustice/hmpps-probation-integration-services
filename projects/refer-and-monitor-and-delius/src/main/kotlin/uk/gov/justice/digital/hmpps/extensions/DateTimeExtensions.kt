package uk.gov.justice.digital.hmpps.extensions

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun ZonedDateTime?.hasChanged(other: ZonedDateTime?): Boolean {
    return (this != null && other == null) || (this == null && other != null) || this?.truncatedTo(ChronoUnit.SECONDS)
        ?.isEqual(other?.truncatedTo(ChronoUnit.SECONDS)) == false
}
