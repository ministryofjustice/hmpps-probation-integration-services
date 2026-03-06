package uk.gov.justice.digital.hmpps.model

enum class DurationUnit(val code: String) {
    HOURS("H"), DAYS("D"), WEEKS("W"), MONTHS("M"), YEARS("Y");

    companion object {
        fun ofCode(code: String) = entries.single { it.code == code }
    }
}
