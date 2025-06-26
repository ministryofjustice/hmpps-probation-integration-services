package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.oasys.assessment.entity.OasysAssessment
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object OasysAssessmentGenerator {
    val DEFAULT = OasysAssessment(
        IdGenerator.getAndIncrement(),
        CaseEntityGenerator.DEFAULT.id,
        LocalDate.now().minusWeeks(10),
        99L,
        ZonedDateTime.of(LocalDateTime.now().minusWeeks(10), EuropeLondon),
        false
    )
}
