package uk.gov.justice.digital.hmpps.integrations.delius

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class ProbationMatchRequest(
    val firstName: String,
    val surname: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dateOfBirth: LocalDate,
    val nomsNumber: String? = null,
    val activeSentence: Boolean = false,
    val pncNumber: String? = null,
    val croNumber: String? = null,
)

data class ProbationMatchResponse(
    val matches: List<OffenderMatch>,
    val matchedBy: String,
)

data class OffenderMatch(
    val offender: OffenderDetail,
)

data class OffenderDetail(
    val otherIds: IDs,
    val previousSurname: String? = null,
    val title: String? = null,
    val firstName: String? = null,
    val middleNames: List<String>? = null,
    val surname: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val currentDisposal: String? = null,
)

data class IDs(
    val crn: String,
    val pncNumber: String? = null,
    val croNumber: String? = null,
    val niNumber: String? = null,
    val nomsNumber: String? = null,
    val immigrationNumber: String? = null,
    val mostRecentPrisonerNumber: String? = null,
)

data class PncNumber(val year: Int, val serial: Int, val checkSum: Char) {
    fun matchValue() = "${fullYear()}/${String.format("%07d", serial)}" + checkSum.uppercase()
    private fun fullYear() = when {
        year > 99 -> year
        year <= LocalDate.now().year % 100 -> "20${formattedYear()}"
        else -> "19${formattedYear()}"
    }

    private fun formattedYear() = String.format("%02d", year)

    companion object {
        val PNC_REGEX = "(\\d{2,4})/?(\\d+)([a-zA-Z])".toRegex()

        fun from(value: String?) = value?.takeIf { it.matches(PNC_REGEX) }?.let {
            val (year, serial, checkSum) = PNC_REGEX.find(it)!!.destructured
            PncNumber(year.toInt(), serial.toInt(), checkSum.first())
        }
    }
}