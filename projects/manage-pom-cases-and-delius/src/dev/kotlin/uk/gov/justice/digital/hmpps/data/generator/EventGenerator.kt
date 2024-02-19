package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.Event
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import uk.gov.justice.digital.hmpps.set
import java.time.LocalDate

object EventGenerator {
    fun generateEvent(
        personId: Long = PersonGenerator.DEFAULT.id,
        disposal: Disposal? = null,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Event(personId, disposal, active, softDeleted, id)

    fun generateDisposal(
        event: Event,
        active: Boolean = true,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Disposal(event, active, softDeleted, id).apply { event.set("disposal", this) }

    fun generateCustody(
        disposal: Disposal,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Custody(disposal, softDeleted, id)

    fun generateKeyDate(
        custody: Custody,
        type: ReferenceData,
        date: LocalDate,
        id: Long = IdGenerator.getAndIncrement()
    ) = KeyDate(custody.id, type, date, id)
}
