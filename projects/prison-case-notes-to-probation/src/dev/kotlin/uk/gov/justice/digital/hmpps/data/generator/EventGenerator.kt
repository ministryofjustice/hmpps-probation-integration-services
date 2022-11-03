package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DisposalType
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event

object EventGenerator {
    const val EXISTING_EVENT_ID = 11111L
    val CUSTODIAL_EVENT = custodialEvent(OffenderGenerator.DEFAULT.id)

    fun custodialEvent(offenderId: Long): Event {
        val event = Event(
            IdGenerator.getAndIncrement(),
            offenderId,
        )
        val disposal = Disposal(
            IdGenerator.getAndIncrement(),
            event,
            DisposalType(IdGenerator.getAndIncrement(), DisposalType.CUSTODIAL_CODES[0])
        )
        return event.copy(disposal = disposal)
    }
}
