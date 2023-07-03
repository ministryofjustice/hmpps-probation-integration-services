package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.District
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Team

object PersonManagerGenerator {
    val CASE_SUMMARY = generate(PersonGenerator.CASE_SUMMARY.id)

    fun generate(personId: Long) = PersonManager(
        id = IdGenerator.getAndIncrement(),
        personId = personId,
        team = Team(
            id = IdGenerator.getAndIncrement(),
            code = "TEAM01",
            description = "Team description",
            district = District(
                id = IdGenerator.getAndIncrement(),
                description = "Local admin unit"
            )
        ),
        staff = Staff(
            id = IdGenerator.getAndIncrement(),
            code = "STAFF01",
            forename = "Forename",
            surname = "Surname"
        ),
        provider = Provider(
            id = IdGenerator.getAndIncrement(),
            code = "TST",
            description = "Provider description"
        )
    )
}
