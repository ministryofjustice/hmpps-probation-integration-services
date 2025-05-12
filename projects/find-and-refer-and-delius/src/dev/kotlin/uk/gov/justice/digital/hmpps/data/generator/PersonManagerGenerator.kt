package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_1
import uk.gov.justice.digital.hmpps.entity.*

object PersonManagerGenerator {

    val DEFAULT_BOROUGH = Borough(IdGenerator.getAndIncrement(), "A", "Test PDU")
    val DEFAULT_DISTRICT = District(IdGenerator.getAndIncrement(), DEFAULT_BOROUGH)
    val DEFAULT_TEAM = Team(IdGenerator.getAndIncrement(), DEFAULT_DISTRICT)
    val PERSON_MANAGER = generatePersonManager(PERSON_1)

    private fun generatePersonManager(person: Person) =
        PersonManager(IdGenerator.getAndIncrement(), person, DEFAULT_TEAM, false, true)
}
