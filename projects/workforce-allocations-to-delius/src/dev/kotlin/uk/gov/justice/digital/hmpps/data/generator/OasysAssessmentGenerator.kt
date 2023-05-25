package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.event.ogrs.OASYSAssessment
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object OasysAssessmentGenerator {
    val DEFAULT = OASYSAssessment(
        IdGenerator.getAndIncrement(),
        PersonGenerator.DEFAULT.id,
        EventGenerator.DEFAULT.number,
        LocalDate.now().minusWeeks(10),
        99L,
        ZonedDateTime.of(LocalDateTime.now().minusWeeks(10), EuropeLondon),
        false
    )
}
