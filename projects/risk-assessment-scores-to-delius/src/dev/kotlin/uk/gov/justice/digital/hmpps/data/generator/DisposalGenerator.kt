package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Disposal
import java.time.ZonedDateTime

object DisposalGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(
        id,
        EventGenerator.DEFAULT,
        DisposalTypeGenerator.DEFAULT,
        disposalDate = ZonedDateTime.now().minusMonths(5)
    )
}
