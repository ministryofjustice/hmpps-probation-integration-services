package uk.gov.justice.digital.hmpps.enum

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

enum class RiskOfSeriousHarmType(val code: String) {
    L("RLRH"),
    M("RMRH"),
    H("RHRH"),
    V("RVHR");

    companion object {
        fun of(value: String): RiskOfSeriousHarmType? = entries.firstOrNull { it.name.equals(value, true) }
    }
}

enum class RiskOfSeriousHarmTypeName(val code: String) {
    @JsonProperty("Low")
    L("RLRH"),

    @JsonProperty("Medium")
    M("RMRH"),

    @JsonProperty("High")
    H("RHRH"),

    @JsonProperty("Very High")
    @JsonAlias("Very high")
    V("RVHR");

    companion object {
        fun of(value: String): RiskOfSeriousHarmTypeName? = entries.firstOrNull { it.name.equals(value, true) }
    }
}