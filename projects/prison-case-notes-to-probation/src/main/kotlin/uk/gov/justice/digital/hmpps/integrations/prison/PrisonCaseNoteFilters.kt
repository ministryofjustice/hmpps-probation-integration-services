package uk.gov.justice.digital.hmpps.integrations.prison

object PrisonCaseNoteFilters {
    val filters: List<PrisonCaseNoteFilter> = listOf(
        PrisonCaseNoteFilter("case note text is empty") {
            it.text.isBlank()
        },
        PrisonCaseNoteFilter("Prisoner being transferred") {
            it.locationId == "TRN"
        },
        PrisonCaseNoteFilter("Institution used for test purposes") {
            it.locationId == "ZZGHI"
        },
        PrisonCaseNoteFilter("Filtered due to data sharing restrictions") {
            it.text.contains("Do not share with offender and OCG Nominal")
        },
    )
}

data class PrisonCaseNoteFilter(
    val reason: String,
    val predicate: (PrisonCaseNote) -> Boolean
)
