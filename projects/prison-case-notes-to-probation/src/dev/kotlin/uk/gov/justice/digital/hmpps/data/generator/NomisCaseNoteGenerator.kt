package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.nomis.NomisCaseNote
import java.time.ZonedDateTime

object NomisCaseNoteGenerator {
    val EXISTING_IN_BOTH = NomisCaseNote(
        eventId = 11111,
        offenderIdentifier = OffenderGenerator.DEFAULT.nomsId,
        type = CaseNoteNomisTypeGenerator.DEFAULT.nomisCode,
        subType = "IEP_WARN",
        creationDateTime = ZonedDateTime.parse("2019-04-16T11:22:33+01:00"),
        occurrenceDateTime = ZonedDateTime.parse("2019-03-23T11:22:00+01:00"),
        authorName = "Some Name",
        text = "note content",
        locationId = "LEI",
        amendments = listOf()
    )

    val NEW_TO_DELIUS = NomisCaseNote(
        eventId = 22222,
        offenderIdentifier = OffenderGenerator.DEFAULT.nomsId,
        type = CaseNoteNomisTypeGenerator.DEFAULT.nomisCode,
        subType = "IEP_WARN",
        creationDateTime = ZonedDateTime.parse("2019-04-16T11:22:33+01:00"),
        occurrenceDateTime = ZonedDateTime.parse("2019-03-23T11:22:00+01:00"),
        authorName = "Some Name",
        text = "note content",
        locationId = "LEI",
        amendments = listOf()
    )
}