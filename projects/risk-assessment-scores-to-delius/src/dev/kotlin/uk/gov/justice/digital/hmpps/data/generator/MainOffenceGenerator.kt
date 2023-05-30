package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.MainOffence
import java.time.LocalDate

object MainOffenceGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = MainOffence(id, EventGenerator.DEFAULT, OffenceGenerator.DEFAULT, date = LocalDate.now(), offenceCount = 5)
}
