package uk.gov.justice.digital.hmpps.config

import uk.gov.justice.digital.hmpps.client.Offence

data class IgnoredOffence(
    val reason: String,
    val matches: (offence: Offence) -> Boolean
) {
    companion object {
        val IGNORED_OFFENCES = listOf(
            IgnoredOffence("Offence is expired") {
                it.endDate != null
            },
            IgnoredOffence("Home Office Code is 'Not Known'") {
                it.homeOfficeCode == "22222"
            },
            IgnoredOffence("Not an actual offence") {
                it.highLevelCode == "59800"
            },
            IgnoredOffence("CJS Code suffix is 500 or above") {
                it.code.takeLast(3).toIntOrNull()?.let { suffix -> suffix >= 500 } == true
            },
        )
    }
}