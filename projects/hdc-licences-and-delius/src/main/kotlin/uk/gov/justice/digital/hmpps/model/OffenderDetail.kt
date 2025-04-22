package uk.gov.justice.digital.hmpps.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenderDetail(
    val otherIds: IDs,
    val offenderManagers: List<OffenderManager>? = null
)

data class IDs(
    @Schema(example = "A123456", description = "Probation Case Reference Number")
    val crn: String,
    @Schema(example = "G5555TT", description = "Prison Offender Number")
    val nomsNumber: String
)

data class OffenderManager(
    val staff: StaffHuman? = null,
    val probationArea: ProbationArea,
    val active: Boolean
)

data class StaffHuman(
    @Schema(description = "Staff code", example = "AN001A") val code: String,
    @Schema(description = "Given names", example = "Sheila Linda") val forenames: String,
    @Schema(description = "Family name", example = "Hancock") val surname: String,
    @Schema(description = "When true the not allocated", example = "false") val unallocated: Boolean,
)

data class ProbationArea(
    val description: String? = null
)

