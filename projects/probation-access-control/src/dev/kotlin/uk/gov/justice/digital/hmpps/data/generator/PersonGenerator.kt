package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.LimitedAccessPerson

object PersonGenerator {
    val DEFAULT = generateLaoPerson(crn = "X123456")
    val EXCLUDED = generateLaoPerson(crn = "E123456", exclusionMessage = "This case is excluded.")
    val RESTRICTED = generateLaoPerson(crn = "R123456", restrictionMessage = "This case is restricted.")
    val BOTH = generateLaoPerson(
        crn = "B123456",
        exclusionMessage = "This case is excluded.",
        restrictionMessage = "This case is restricted."
    )

    private fun generateLaoPerson(
        id: Long = IdGenerator.getAndIncrement(),
        crn: String,
        exclusionMessage: String? = null,
        restrictionMessage: String? = null,
    ) = LimitedAccessPerson(crn, exclusionMessage, restrictionMessage, id)
}