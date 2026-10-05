package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.entity.ContactType

object ContactTypeGenerator {
    val DEFAULT = generate()
    val HOME_VISIT_TO_CASE_NS = generate(code = "CHVS", homeVisit = true)
    val INITIAL_APPOINTMENT_HOME_VISIT_NS = generate(code = "COHV", homeVisit = true)

    fun generate(
        code: String = "TEST",
        homeVisit: Boolean? = null,
        id: Long = IdGenerator.getAndIncrement(),
    ) = ContactType(
        id = id,
        code = code,
        homeVisit = homeVisit,
    )
}


