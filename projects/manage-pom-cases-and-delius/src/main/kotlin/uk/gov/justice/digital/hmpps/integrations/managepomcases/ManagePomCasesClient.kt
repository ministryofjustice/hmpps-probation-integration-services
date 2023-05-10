package uk.gov.justice.digital.hmpps.integrations.managepomcases

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI
import java.time.LocalDate

@FeignClient(name = "manage-pom-cases", url = "https://dummy-url/to/be/overridden")
interface ManagePomCasesClient {
    @GetMapping
    fun getDetails(url: URI): Handover?
}

data class Handover(
    @JsonAlias("nomsNumber") val nomsId: String,
    @JsonAlias("handoverDate") val date: LocalDate,
    @JsonAlias("handoverStartDate") val startDate: LocalDate?
)
