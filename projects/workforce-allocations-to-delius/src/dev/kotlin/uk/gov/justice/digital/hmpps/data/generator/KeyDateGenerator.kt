package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.entity.KeyDate
import java.time.LocalDate

object KeyDateGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = KeyDate(
        id,
        CustodyGenerator.DEFAULT,
        ReferenceDataGenerator.KEY_DATE_EXP_REL_DATE,
        LocalDate.now().plusWeeks(1)
    )
}
