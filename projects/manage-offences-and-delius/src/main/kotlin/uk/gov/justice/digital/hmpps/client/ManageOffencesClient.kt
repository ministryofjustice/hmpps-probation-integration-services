package uk.gov.justice.digital.hmpps.client

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import java.time.LocalDate

interface ManageOffencesClient {
    @GetExchange(value = "/offences/code/unique/{code}")
    fun getOffence(@PathVariable code: String): Offence
}

data class Offence(
    val code: String,
    val description: String,
    val offenceType: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val homeOfficeStatsCode: String? = null,
    val homeOfficeDescription: String? = null,
    val legislation: String? = null,
    val schedules: List<Schedule> = listOf(),
) {
    val schedule15ViolentOffence: Boolean =
        schedules.any { it.code == Schedule.FIFTEEN && it.partNumber == Schedule.VIOLENT_PART }

    val schedule15SexualOffence: Boolean =
        schedules.any { it.code == Schedule.FIFTEEN && it.partNumber == Schedule.SEXUAL_PART }
}

data class Schedule(val code: String, val partNumber: String) {
    companion object {
        const val FIFTEEN = "15"
        const val VIOLENT_PART = "1"
        const val SEXUAL_PART = "2"
    }
}
