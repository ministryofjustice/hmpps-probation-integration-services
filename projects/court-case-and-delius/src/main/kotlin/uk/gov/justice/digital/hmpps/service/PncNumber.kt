package uk.gov.justice.digital.hmpps.service

import java.time.LocalDate.now

data class PncNumber(val year: Int, val serial: Int, val checkSum: Char) {
    fun matchValue() = "${fullYear()}/${String.format("%07d", serial)}" + checkSum.uppercase()
    private fun fullYear() = when {
        year > 99 -> year
        year % 100 <= now().year % 100 -> "20${formattedYear()}"
        else -> "19${formattedYear()}"
    }

    private fun formattedYear() = String.format("%02d", year)

    companion object {
        val PNC_REGEX = "(\\d{2,4})/(\\d+)([a-zA-Z])".toRegex()

        fun from(value: String?) = value?.takeIf { it.matches(PNC_REGEX) }?.let {
            val (year, serial, checkSum) = PNC_REGEX.find(it)!!.destructured
            PncNumber(year.toInt(), serial.toInt(), checkSum.first())
        }
    }
}