package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event
import java.time.LocalDate
import java.time.ZonedDateTime

object DisposalGenerator {
    val DEFAULT = generate(EventGenerator.DEFAULT)
    val NEP_DISPOSAL_3 = generate(EventGenerator.NEP_3, terminationDate = LocalDate.now())
    val NEP_DISPOSAL_2 = generate(EventGenerator.NEP_2)

    fun generate(
        event: Event,
        disposalDate: ZonedDateTime = ZonedDateTime.now().minusMonths(5),
        terminationDate: LocalDate? = null,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(
        id,
        event,
        DisposalTypeGenerator.DEFAULT,
        disposalDate = disposalDate,
        terminationDate = terminationDate,
    )
}
