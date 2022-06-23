package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import java.time.ZonedDateTime
import kotlin.random.Random

object CaseNoteGenerator {
    val EXISTING = generate(CaseNoteMessageGenerator.EXISTS_IN_DELIUS.eventId)

    fun generate(
        nomisId: Long = Random.nextLong(),
        offenderId: Long = OffenderGenerator.DEFAULT.id,
        type: CaseNoteType = CaseNoteNomisTypeGenerator.DEFAULT.type,
        description: String = "Another case note from Nomis",
        probationAreaId: Long = ProbationAreaGenerator.DEFAULT.id,
        teamId: Long = TeamGenerator.DEFAULT.id,
        staffId: Long = StaffGenerator.DEFAULT.id,
    ): CaseNote {
        val now = ZonedDateTime.now()
        return CaseNote(
            IdGenerator.getAndIncrement(),
            offenderId,
            nomisId,
            type,
            description,
            now,
            now,
            staffId,
            staffId,
            teamId,
            probationAreaId,
            createdDateTime = now,
            lastModifiedDateTime = now,
            createdByUserId = UserGenerator.APPLICATION_USER.id,
            lastModifiedUserId = UserGenerator.APPLICATION_USER.id,
        )
    }
}