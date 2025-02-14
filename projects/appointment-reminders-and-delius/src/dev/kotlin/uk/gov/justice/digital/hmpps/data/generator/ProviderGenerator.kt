package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ProbationArea

object ProviderGenerator {
    val LONDON = ProbationArea(IdGenerator.getAndIncrement(), "N07", "London")
    val WALES = ProbationArea(IdGenerator.getAndIncrement(), "N03", "Wales")
}
