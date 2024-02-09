package uk.gov.justice.digital.hmpps.integrations.cvl

import com.fasterxml.jackson.annotation.JsonAlias
import org.hibernate.sql.Restriction
import org.springframework.web.service.annotation.GetExchange
import java.net.URI
import java.time.LocalDate

interface CvlClient {
    @GetExchange
    fun getActivatedLicence(uri: URI): ActivatedLicence?
}

data class ActivatedLicence(
    val crn: String,
    @JsonAlias("licenceStartDate")
    val startDate: LocalDate,
    val conditions: Conditions
)

interface Describable {
    val description: String
}

data class StandardLicenceCondition(
    val code: String,
    @JsonAlias("text")
    override val description: String,
) : Describable

data class AdditionalLicenceCondition(
    val type: Type,
    val code: String,
    @JsonAlias("text")
    override val description: String,
    val restrictions: List<Restriction>?
) : Describable {
    enum class Type {
        ELECTRONIC_MONITORING, MULTIPLE_EXCLUSION_ZONE, STANDARD
    }

    enum class Restriction(val modifier: String) {
        ALCOHOL_ABSTINENCE("alcohol abstinence"),
        ALCOHOL_MONITORING("alcohol monitoring"),
        ATTENDANCE_AT_APPOINTMENTS("attendance at appointments"),
        CURFEW("curfew"),
        EXCLUSION_ZONE("exclusion zone"),
        LOCATION_MONITORING("location monitoring")
    }
}

data class BespokeLicenceCondition(
    @JsonAlias("text")
    override val description: String
) : Describable

data class Conditions(
    @JsonAlias("AP")
    val ap: ApConditions
)

data class ApConditions(
    val standard: List<StandardLicenceCondition>,
    val additional: List<AdditionalLicenceCondition>,
    val bespoke: List<BespokeLicenceCondition>
)

fun ActivatedLicence.telemetryProperties(eventNumber: String): Map<String, String> = mapOf(
    "crn" to crn,
    "eventNumber" to eventNumber,
    "startDate" to startDate.toString(),
    "standardConditions" to conditions.ap.standard.size.toString(),
    "additionalConditions" to conditions.ap.additional.size.toString(),
    "bespokeConditions" to conditions.ap.bespoke.size.toString()
)
