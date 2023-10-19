package uk.gov.justice.digital.hmpps.integrations.cvl

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI
import java.time.LocalDate

@FeignClient(name = "create-and-vary-a-licence", url = "https://dummy-url/to/be/overridden")
interface CvlClient {
    @GetMapping
    fun getActivatedLicence(uri: URI): ActivatedLicence?
}

data class ActivatedLicence(
    val crn: String,
    val releaseDate: LocalDate,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val standardLicenceConditions: List<StandardLicenceCondition>,
    val additionalLicenceConditions: List<AdditionalLicenceCondition>,
    val bespokeLicenceConditions: List<BespokeLicenceCondition>
)

interface Describable {
    val description: String
}

data class StandardLicenceCondition(
    val code: String,
    override val description: String,
    val pssCondition: Boolean
) : Describable

data class AdditionalLicenceCondition(
    val code: String,
    override val description: String,
    val pssCondition: Boolean
) : Describable

data class BespokeLicenceCondition(
    override val description: String
) : Describable

fun ActivatedLicence.telemetryProperties(eventNumber: String): Map<String, String> = mapOf(
    "crn" to crn,
    "eventNumber" to eventNumber,
    "releaseDate" to releaseDate.toString(),
    "standardConditions" to standardLicenceConditions.size.toString(),
    "additionalConditions" to additionalLicenceConditions.size.toString(),
    "bespokeConditions" to bespokeLicenceConditions.size.toString()
)
