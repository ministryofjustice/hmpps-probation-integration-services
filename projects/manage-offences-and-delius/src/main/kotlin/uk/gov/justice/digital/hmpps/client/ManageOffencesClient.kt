package uk.gov.justice.digital.hmpps.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import uk.gov.justice.digital.hmpps.config.FeignOAuth2Config
import java.time.LocalDate

@FeignClient(
    name = "manage-offences",
    url = "\${integrations.manage-offences.url}",
    configuration = [FeignOAuth2Config::class]
)
interface ManageOffencesClient {
    @GetMapping(value = ["/offences/code/{code}"])
    fun getOffence(@PathVariable code: String): List<Offence>
}

data class Offence(
    val code: String,
    val description: String? = null,
    val offenceType: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val homeOfficeStatsCode: String? = null,
    val legislation: String? = null
)
