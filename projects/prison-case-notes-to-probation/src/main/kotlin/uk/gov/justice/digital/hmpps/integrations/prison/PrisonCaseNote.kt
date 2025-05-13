package uk.gov.justice.digital.hmpps.integrations.prison

import com.fasterxml.jackson.annotation.JsonAlias
import uk.gov.justice.digital.hmpps.datetime.DeliusDateTimeFormatter
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteBody
import uk.gov.justice.digital.hmpps.integrations.delius.model.CaseNoteHeader
import uk.gov.justice.digital.hmpps.integrations.delius.model.DeliusCaseNote
import uk.gov.justice.digital.hmpps.model.StaffName
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

const val UNKNOWN_LOCATION = "UNK"

data class PrisonCaseNote(
    @JsonAlias("caseNoteId")
    val id: UUID,
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
    fun getStaffName(): StaffName {
        val name = authorName.trim().split("\\s+".toRegex())
        return StaffName(name.first(), name.last())
    }

    fun occurredAt(): ZonedDateTime = if (type == "ALERT") {
        /*
        * alert type is generated as a side affect of creating an alert
        * no time component is stored in nomis or dps
        * it is artificially generated here using created at in order to allow a fake order in delius ui
        * until the alerts integration is defined and delivered
        * */
        val date = occurrenceDateTime.toLocalDate()
        val time = creationDateTime.toLocalTime()
        ZonedDateTime.of(LocalDateTime.of(date, time), EuropeLondon)
    } else {
        occurrenceDateTime
    }
}

data class CaseNoteAmendment(
    val creationDateTime: ZonedDateTime?,
    val authorName: String,
    val additionalNoteText: String
)

fun PrisonCaseNote.toDeliusCaseNote(): DeliusCaseNote {
    fun amendments(): (CaseNoteAmendment) -> String = { a ->
        "${System.lineSeparator()}[${a.authorName} updated the case notes on ${DeliusDateTimeFormatter.format(a.creationDateTime)}]${System.lineSeparator()}${a.additionalNoteText}"
    }

    return DeliusCaseNote(
        header = CaseNoteHeader(offenderIdentifier, eventId, id, CaseNoteHeader.Type.CaseNote),
        body = CaseNoteBody(
            type = type,
            subType = subType,
            content = text + amendments.joinToString(separator = "", transform = amendments()),
            contactTimeStamp = occurredAt(),
            systemTimestamp = amendments.mapNotNull { it.creationDateTime }.maxOrNull() ?: creationDateTime,
            staffName = getStaffName(),
            establishmentCode = locationId,
            alertDescription = if (type == "ALERT") text.lines().first() else ""
        )
    )
}
