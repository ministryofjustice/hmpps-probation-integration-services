package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Team

object TeamGenerator {
    val DEFAULT = Team(IdGenerator.getAndIncrement(), "${ProbationAreaGenerator.DEFAULT.code}CSN")
}