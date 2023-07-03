package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.controller.entity.OGRSAssessment
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object OgrsAssessmentGenerator {
    val DEFAULT = OGRSAssessment(
        IdGenerator.getAndIncrement(),
        LocalDate.now().minusWeeks(1),
        EventGenerator.DEFAULT,
        88L,
        ZonedDateTime.of(LocalDateTime.now().minusWeeks(1), EuropeLondon),
        false
    )
}
