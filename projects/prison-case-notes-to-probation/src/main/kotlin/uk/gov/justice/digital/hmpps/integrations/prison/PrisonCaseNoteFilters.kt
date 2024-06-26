package uk.gov.justice.digital.hmpps.integrations.prison

object PrisonCaseNoteFilters {
    val filters: List<PrisonCaseNoteFilter> = listOf(
        PrisonCaseNoteFilter("case note text is empty") {
            it.text.isNullOrBlank()
        },
        PrisonCaseNoteFilter("Prisoner being transferred") {
            it.locationId == "TRN"
        },
        PrisonCaseNoteFilter("Filtered due to data sharing restrictions") {
            it.text?.contains("Do not share with offender and OCG Nominal") == true
        },
    )
}

data class PrisonCaseNoteFilter(
    val reason: String,
    val predicate: (PrisonCaseNote) -> Boolean
)
