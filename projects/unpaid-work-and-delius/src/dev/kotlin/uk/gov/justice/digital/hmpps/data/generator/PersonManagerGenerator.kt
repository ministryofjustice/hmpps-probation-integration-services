package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.common.entity.person.PersonWithManager

object PersonManagerGenerator {
    val DEFAULT = generate()
    fun generate(
        code: String = "STAFF1",
        id: Long = IdGenerator.getAndIncrement()
    ) = PersonManager(
        id,
        PersonWithManager(
            CaseGenerator.DEFAULT.id,
            CaseGenerator.DEFAULT.crn
        ),
        StaffGenerator.DEFAULT, TeamGenerator.DEFAULT
    )
}
