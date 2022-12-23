package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.recommendation.person.entity.PersonManager
import uk.gov.justice.digital.hmpps.set

object PersonGenerator {
    val RECOMMENDATION_STARTED = generate("X08769")
    val DECISION_TO_RECALL = generate("X08770")
    val DECISION_NOT_TO_RECALL = generate("X08771")

    fun generate(
        crn: String,
        providerId: Long = 1,
        teamId: Long = 2,
        staffId: Long = 3,
        id: Long = IdGenerator.getAndIncrement()
    ): Person {
        val person = Person(id, crn)
        val personManager = PersonManager(IdGenerator.getAndIncrement(), person, providerId, teamId, staffId)
        person.set("manager", personManager)
        return person
    }
}
