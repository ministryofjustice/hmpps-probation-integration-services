package uk.gov.justice.digital.hmpps.enum

enum class RiskOfSeriousHarmType(val code: String) {
    L("RLRH"), M("RMRH"), H("RHRH"), V("RVHR");

    companion object {
        fun of(value: String): RiskOfSeriousHarmType? = entries.firstOrNull { it.name.equals(value, true) }
    }
}