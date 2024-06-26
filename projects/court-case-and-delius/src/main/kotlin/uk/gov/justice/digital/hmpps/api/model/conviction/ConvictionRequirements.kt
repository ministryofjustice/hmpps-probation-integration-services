package uk.gov.justice.digital.hmpps.api.model.conviction

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import java.time.LocalDate

data class ConvictionRequirements(
    val requirements: List<Requirement>
)

data class Requirement(
    @Schema(description = "Unique identifier for the requirement", required = true)
    val requirementId: Long,
    @Schema(description = "Name of the requirement", required = true)
    val requirementNotes: String?,
    val commencementDate: LocalDate?,
    val startDate: LocalDate?,
    val terminationDate: LocalDate?,
    val expectedStartDate: LocalDate?,
    val expectedEndDate: LocalDate?,
    @Schema(description = "Is the requirement currently active")
    val active: Boolean,
    val requirementTypeSubCategory: KeyValue?,
    val requirementTypeMainCategory: KeyValue?,
    val adRequirementTypeMainCategory: KeyValue?,
    val adRequirementTypeSubCategory: KeyValue?,
//    val terminationReason: KeyValue,
//    @Schema(description = "The number of temporal units to complete the requirement (see lengthUnit field for unit)")
//    val length: Long,
//    @Schema(description = "The temporal unit corresponding to the length field")
//    val lengthUnit: String,
//    @Schema(description = "Is the main category restrictive")
//    val restrictive: Boolean,
//    val softDeleted: Boolean,
//    @Schema(description = "Total RAR days completed")
//    val rarCount: Long
)