package uk.gov.justice.digital.hmpps.integrations.client

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import java.time.LocalDate

interface ManageOffencesClient {
    @GetExchange(value = "/offences/code/unique/{code}")
    fun getOffenceByCode(@PathVariable code: String): Offence
}

data class Offence(
    val id: Long,
    val code: String,
    val description: String,
    val homeOfficeStatsCode: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null
)