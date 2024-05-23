package uk.gov.justice.digital.hmpps.integrations.prison

object PrisonCaseNoteFilters {
    val filters: List<PrisonCaseNoteFilter> = listOf(
        PrisonCaseNoteFilter("case note text is empty") {
            it.text.isNullOrBlank()
        },
        PrisonCaseNoteFilter("Prisoner being transferred") {
            it.locationId == "TRN"
        },
        PrisonCaseNoteFilter("Filtered as cannot share with offender") {
            it.text?.contains("Filtered due to data sharing restrictions") == true
        },
    )
}

data class PrisonCaseNoteFilter(
    val reason: String,
    val predicate: (PrisonCaseNote) -> Boolean
)
