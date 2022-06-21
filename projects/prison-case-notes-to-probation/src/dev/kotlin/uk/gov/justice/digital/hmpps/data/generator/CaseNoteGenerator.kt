package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import java.time.ZonedDateTime
import kotlin.random.Random

object CaseNoteGenerator {
    val EXISTING = run {
        val now = ZonedDateTime.now()
        CaseNote(
            IdGenerator.getAndIncrement(),
            OffenderGenerator.DEFAULT.id,
            CaseNoteMessageGenerator.EXISTS_IN_DELIUS.eventId,
            CaseNoteNomisTypeGenerator.DEFAULT.type,
            "A Case Note from Nomis",
            now,
            now,
            now,
            UserGenerator.APPLICATION_USER.id,
            UserGenerator.APPLICATION_USER.id,
            now,
            1,
        )
    }

    fun generate(
        nomisId: Long = Random.nextLong(),
        offenderId: Long = OffenderGenerator.DEFAULT.id,
        type: CaseNoteType = CaseNoteNomisTypeGenerator.DEFAULT.type,
        description: String = "Another case note from Nomis"
    ) {
        val now = ZonedDateTime.now()
        CaseNote(
            IdGenerator.getAndIncrement(),
            offenderId,
            nomisId,
            type,
            description,
            now,
            now,
            now,
            UserGenerator.APPLICATION_USER.id,
            UserGenerator.APPLICATION_USER.id,
            now,
            0,
        )
    }
}