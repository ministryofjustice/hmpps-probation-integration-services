package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import java.time.ZonedDateTime
import kotlin.random.Random

object CaseNoteGenerator {
    var EXISTING = generate(CaseNoteMessageGenerator.EXISTS_IN_DELIUS.eventId, description = "A case note from Nomis")

    fun generate(
        nomisId: Long = Random.nextLong(),
        offenderId: Long = OffenderGenerator.DEFAULT.id,
        eventId: Long? = EventGenerator.CUSTODIAL_EVENT.id,
        nsiId: Long? = NsiGenerator.EVENT_CASE_NOTE_NSI.id,
        type: CaseNoteType = CaseNoteTypeGenerator.DEFAULT,
        description: String = "Another case note from Nomis",
        probationAreaId: Long = ProbationAreaGenerator.DEFAULT.id,
        teamId: Long = TeamGenerator.DEFAULT.id,
        staffId: Long = StaffGenerator.DEFAULT.id,
    ): CaseNote {
        val now = ZonedDateTime.now()
        return CaseNote(
            IdGenerator.getAndIncrement(),
            offenderId,
            eventId,
            nsiId,
            nomisId,
            type,
            description,
            now,
            now,
            staffId,
            staffId,
            teamId,
            probationAreaId,
        )
    }
}
