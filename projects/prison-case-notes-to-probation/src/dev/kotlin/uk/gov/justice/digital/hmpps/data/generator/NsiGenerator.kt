package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.entity.NsiType
import java.time.LocalDate

object NsiGenerator {
    val EVENT_CASE_NOTE_NSI = generate(
        eventId = EventGenerator.CUSTODIAL_EVENT.id,
        type = NsiType(IdGenerator.getAndIncrement(), "CaseNoteEventNsiType")
    )

    fun generate(
        offenderId: Long = OffenderGenerator.DEFAULT.id,
        eventId: Long? = null,
        type: NsiType = NsiType(IdGenerator.getAndIncrement(), "CaseNoteNsiType"),
        referralDate: LocalDate = LocalDate.now().minusDays(7)
    ): Nsi {
        return Nsi(
            IdGenerator.getAndIncrement(),
            type,
            referralDate,
            offenderId,
            eventId
        )
    }
}
