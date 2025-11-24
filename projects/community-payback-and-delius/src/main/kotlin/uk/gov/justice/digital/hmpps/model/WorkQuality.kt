package uk.gov.justice.digital.hmpps.model

enum class WorkQuality(val code: String) {
    EXCELLENT("EX"),
    GOOD("GD"),
    NOT_APPLICABLE("NA"),
    POOR("PR"),
    SATISFACTORY("ST"),
    UNSATISFACTORY("US");

    companion object {
        fun of(code: String?): WorkQuality? {
            return WorkQuality.entries.firstOrNull { value -> value.code == code }
        }
    }
}
