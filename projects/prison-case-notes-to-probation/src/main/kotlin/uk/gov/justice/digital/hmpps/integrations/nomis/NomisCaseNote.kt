package uk.gov.justice.digital.hmpps.integrations.nomis

import com.fasterxml.jackson.annotation.JsonAlias
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteBody
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import java.time.ZonedDateTime

const val UNKNOWN_LOCATION = "UNK"

data class NomisCaseNote(
    val eventId: Long,
    val offenderIdentifier: String,
    val type: String,
    val subType: String,
    val creationDateTime: ZonedDateTime,
    val occurrenceDateTime: ZonedDateTime,
    val authorName: String,
    val text: String,
    val locationId: String = UNKNOWN_LOCATION,
    val amendments: List<CaseNoteAmendment>
) {
    fun getAuthorNameWithComma(): String =
        // delius will throw a 400 bad request if it can't find a comma in the author name
        if (authorName.contains(',')) authorName
        else
        // didn't find a comma, so split and change from forename surname to surname, forename
            "${authorName.substringAfterLast(" ")}, ${authorName.substringBeforeLast(" ")}"
}

data class CaseNoteAmendment(
    val creationDateTime: ZonedDateTime?,
    val authorName: String,
    val additionalNoteText: String
)

data class CaseNoteMessage(
    @JsonAlias("offenderIdDisplay")
    val offenderId: String,
    val caseNoteId: Long,
    val eventType: String
)

fun NomisCaseNote.toDeliusCaseNote(): DeliusCaseNote {

    fun amendments(): (CaseNoteAmendment) -> String = { a ->
        " ...[${a.authorName} updated the case notes on ${DeliusDateTimeFormatter.format(a.creationDateTime)}] ${a.additionalNoteText}"
    }

    return DeliusCaseNote(
        header = CaseNoteHeader(offenderIdentifier, eventId),
        body = CaseNoteBody(
            type = type,
            subType = subType,
            content = text + amendments.joinToString(separator = "", transform = amendments()),
            contactTimeStamp = occurrenceDateTime,
            systemTimeStamp = amendments.mapNotNull { it.creationDateTime }.maxOrNull() ?: creationDateTime,
            staffName = getAuthorNameWithComma(),
            establishmentCode = locationId
        )
    )
}

