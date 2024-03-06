package uk.gov.justice.digital.hmpps.api.model

import java.awt.Color
import java.time.LocalDate

data class RiskSummary(
    val rosh: RiskItem? = null,
    val alerts: RiskItem? = null,
    val safeguarding: RiskItem? = null,
    val information: RiskItem? = null,
    val publicProtection: RiskItem? = null,
)

data class RiskItem(
    val description: String,
    val colour: Colour
)

enum class Colour(val priority: Int) {
    RED(1),
    AMBER(2),
    GREEN(3),
    WHITE(4);
    companion object{
        fun of(colour: String) = checkNotNull( entries.firstOrNull{it.name.equals(colour, true)})
    }
}

enum class RegisterFlag(val code: String) {
    ROSH("1"),
    ALERTS("2"),
    SAFEGUARDING("3"),
    INFORMATION("4"),
    PUBLIC_PROTECTION("5")
}