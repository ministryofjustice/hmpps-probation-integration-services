package uk.gov.justice.digital.hmpps.integrations.prison

object PrisonCaseNoteFilters {
    val filters: List<PrisonCaseNoteFilter> =
        listOf(
            PrisonCaseNoteFilter("case note text is empty") {
                it.text.isNullOrBlank()
            },
            PrisonCaseNoteFilter("Prisoner being transferred") {
                it.locationId == "TRN"
            },
        )
}

data class PrisonCaseNoteFilter(
    val reason: String,
    val predicate: (PrisonCaseNote) -> Boolean,
)
