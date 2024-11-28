package uk.gov.justice.digital.hmpps.enum

enum class RiskLevel(val code: String) {
    L("RSKL"), M("RSKM"), H("RSKH"), V("RSKV");

    companion object {
        fun of(value: String): RiskLevel? = entries.firstOrNull { it.name.equals(value.first().toString(), true) }

        fun of(vararg values: String?): RiskLevel? =
            values.filterNotNull().mapNotNull(RiskLevel::of).maxByOrNull { it.ordinal }
    }
}