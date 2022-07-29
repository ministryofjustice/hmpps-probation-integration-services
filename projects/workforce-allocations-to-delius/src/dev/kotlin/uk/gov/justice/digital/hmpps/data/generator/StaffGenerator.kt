package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff

object StaffGenerator {
    var DEFAULT = generate(
        "${TeamGenerator.DEFAULT.code}U",
        "Unallocated",
        "Staff",
    )

    fun generate(
        code: String,
        forename: String,
        surname: String,
        id: Long = IdGenerator.getAndIncrement(),
    ): Staff {
        return Staff(
            id,
            code,
            forename,
            surname,
            null,
        )
    }
}
