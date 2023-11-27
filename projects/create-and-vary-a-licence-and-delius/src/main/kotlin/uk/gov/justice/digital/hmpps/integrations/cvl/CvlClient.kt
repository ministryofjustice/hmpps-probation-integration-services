package uk.gov.justice.digital.hmpps.integrations.cvl

import org.springframework.web.service.annotation.GetExchange
import java.net.URI
import java.time.LocalDate

interface CvlClient {
    @GetExchange
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
