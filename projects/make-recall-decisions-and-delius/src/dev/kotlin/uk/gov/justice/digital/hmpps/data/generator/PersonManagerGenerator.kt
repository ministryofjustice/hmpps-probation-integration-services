package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.PersonManager

object PersonManagerGenerator {
    val CASE_SUMMARY = generate(PersonGenerator.CASE_SUMMARY.id)

    fun generate(personId: Long) = PersonManager(
        id = IdGenerator.getAndIncrement(),
        personId = personId,
        team = PersonGenerator.DEFAULT_TEAM,
        staff = PersonGenerator.DEFAULT_STAFF,
        provider = PersonGenerator.DEFAULT_PROVIDER
    )
}
