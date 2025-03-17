package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integration.delius.entity.Person

object PersonGenerator {
    val DEFAULT = generate("A000001", "A0001DY")
    val EXCLUSION = generate("E123456", exclusionMessage = "There is an exclusion on this person")
    val RESTRICTION = generate("R123456", restrictionMessage = "There is a restriction on this person")
    val RESTRICTION_EXCLUSION = generate(
        "B123456",
        exclusionMessage = "You are excluded from viewing this case",
        restrictionMessage = "You are restricted from viewing this case"
    )

    fun generate(
        crn: String,
        nomsId: String? = null,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Person(crn, nomsId, exclusionMessage, restrictionMessage, softDeleted, id)
}