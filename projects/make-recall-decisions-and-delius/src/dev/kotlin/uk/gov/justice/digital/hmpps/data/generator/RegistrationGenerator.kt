package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.Registration
import uk.gov.justice.digital.hmpps.integrations.delius.casesummary.RegistrationType

object RegistrationGenerator {
    val MAPPA = generate(PersonGenerator.CASE_SUMMARY.id, "MAPPA 1")
    val HIGH_ROSH = generate(PersonGenerator.CASE_SUMMARY.id, "High RoSH")

    fun generate(personId: Long, type: String) = Registration(
        id = IdGenerator.getAndIncrement(),
        personId = personId,
        type = RegistrationType(
            id = IdGenerator.getAndIncrement(),
            description = type
        )
    )
}
