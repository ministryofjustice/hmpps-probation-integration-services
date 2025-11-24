package uk.gov.justice.digital.hmpps.model

enum class Behaviour(val code: String) {
    EXCELLENT("EX"),
    GOOD("GD"),
    NOT_APPLICABLE("NA"),
    POOR("PR"),
    SATISFACTORY("SA"),
    UNSATISFACTORY("UN");

    companion object {
        fun of(code: String?): Behaviour? {
            return Behaviour.entries.firstOrNull { value -> value.code == code }
        }
    }
}
