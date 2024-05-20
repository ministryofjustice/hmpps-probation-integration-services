package uk.gov.justice.digital.hmpps.integrations.prison

object PrisonCaseNoteFilters {
    val filters: List<PrisonCaseNoteFilter> = listOf(
        PrisonCaseNoteFilter("case note text is empty") {
            it.text.isNullOrBlank()
        },
        PrisonCaseNoteFilter("Prisoner being transferred") {
            it.locationId == "TRN"
        },
        PrisonCaseNoteFilter("OCG Alert Security") {
            it.text == "Alert Security. Do not share with offender and OCG Nominal - Do not share made active."
        },
    )
}

data class PrisonCaseNoteFilter(
    val reason: String,
    val predicate: (PrisonCaseNote) -> Boolean
)
