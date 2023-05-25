package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.entity.CaseNoteType
import java.time.ZonedDateTime
import kotlin.random.Random

object CaseNoteGenerator {
    var EXISTING = generate(
        EventGenerator.EXISTING_EVENT_ID,
        description = """
            NEG IEP_WARN
            This is an existing case note
            [Mickey Mouse updated the case notes on 2022/07/20 12:25:20]
            Some Additional Text
        """.trimIndent(),
        startDateTime = ZonedDateTime.parse("2022-07-20T11:24:10+00:00"),
        lastModifiedDateTime = ZonedDateTime.parse("2022-07-20T11:24:10+00:00")
    )

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
        startDateTime: ZonedDateTime = ZonedDateTime.now(),
        createdDateTime: ZonedDateTime = ZonedDateTime.now(),
        lastModifiedDateTime: ZonedDateTime = ZonedDateTime.now()
    ): CaseNote {
        return CaseNote(
            IdGenerator.getAndIncrement(),
            offenderId,
            eventId,
            nsiId,
            nomisId,
            type,
            description,
            startDateTime,
            startDateTime,
            staffId,
            staffId,
            teamId,
            probationAreaId,
            createdDateTime = createdDateTime,
            lastModifiedDateTime = lastModifiedDateTime
        )
    }
}
