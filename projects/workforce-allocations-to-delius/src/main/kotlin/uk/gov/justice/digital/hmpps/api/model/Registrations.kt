package uk.gov.justice.digital.hmpps.api.model

import java.awt.Color
import java.time.LocalDate

data class RiskSummary(
    val rosh: RiskItem?
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
}