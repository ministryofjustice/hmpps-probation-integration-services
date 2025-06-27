package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.ogrs.entity.OgrsAssessment
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object OgrsAssessmentGenerator {
    val DEFAULT = OgrsAssessment(
        IdGenerator.getAndIncrement(),
        LocalDate.now().minusWeeks(1),
        EventGenerator.DEFAULT,
        88L,
        ZonedDateTime.of(LocalDateTime.now().minusWeeks(1), EuropeLondon),
        false
    )
}
