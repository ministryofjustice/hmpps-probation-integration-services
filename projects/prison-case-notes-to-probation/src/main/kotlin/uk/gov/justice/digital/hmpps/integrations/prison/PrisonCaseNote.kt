package uk.gov.justice.digital.hmpps.integrations.prison

import com.fasterxml.jackson.annotation.JsonAlias
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteBody
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.integrations.delius.model.StaffName
import java.time.ZonedDateTime

const val UNKNOWN_LOCATION = "UNK"

data class PrisonCaseNote(
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
    fun getStaffName(): StaffName =
        if (authorName.contains(',')) {
            StaffName(authorName.substringAfterLast(","), authorName.substringBeforeLast(","))
        } else
            StaffName(authorName.substringBeforeLast(" "), authorName.substringAfterLast(" "))
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
    val eventId: Long,
    val eventType: String,
)

fun PrisonCaseNote.toDeliusCaseNote(): DeliusCaseNote {

    fun amendments(): (CaseNoteAmendment) -> String = { a ->
        "${System.lineSeparator()}[${a.authorName} updated the case notes on ${DeliusDateTimeFormatter.format(a.creationDateTime)}]${System.lineSeparator()}${a.additionalNoteText}"
    }

    return DeliusCaseNote(
        header = CaseNoteHeader(offenderIdentifier, eventId),
        body = CaseNoteBody(
            type = type,
            subType = subType,
            content = text + amendments.joinToString(separator = "", transform = amendments()),
            contactTimeStamp = occurrenceDateTime,
            systemTimeStamp = amendments.mapNotNull { it.creationDateTime }.maxOrNull() ?: creationDateTime,
            staffName = getStaffName(),
            establishmentCode = locationId
        )
    )
}
