package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate
import java.time.LocalDateTime

data class LicenceConditions(
    val licenceConditions: List<LicenceCondition> = emptyList()
)

data class LicenceCondition(
    val licenceConditionNotes: String? = null,
    val startDate: LocalDate,
    val commencementDate: LocalDate? = null,
    val commencementNotes: String? = null,
    val terminationDate: LocalDate? = null,
    val terminationNotes: String? = null,
    val createdDateTime: LocalDateTime,
    val active: Boolean,
    val licenceConditionTypeMainCat: KeyValue? = null,
    val licenceConditionTypeSubCat: KeyValue? = null

)
