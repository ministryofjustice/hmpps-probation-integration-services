package uk.gov.justice.digital.hmpps.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffenderDetail(
    val otherIds: IDs,
    val softDeleted: Boolean? = null,
    val offenderManagers: List<OffenderManager>? = null
)

data class IDs(
    @Schema(example = "A123456", description = "Probation Case Reference Number")
    val crn: String,
    @Schema(example = "G5555TT", description = "Prison Offender Number")
    val nomsNumber: String? = null
)

data class OffenderManager(
    val staff: StaffHuman? = null,
    val softDeleted: Boolean? = null,
    val probationArea: ProbationArea? = null,
    val active: Boolean? = null,
)

data class StaffHuman(
    @Schema(description = "Staff code", example = "AN001A") val code: String? = null,
    @Schema(description = "Given names", example = "Sheila Linda") val forenames: String? = null,
    @Schema(description = "Family name", example = "Hancock") val surname: String? = null,
    @Schema(description = "When true the not allocated", example = "false") val unallocated: Boolean? = null,
)

data class ProbationArea(
    val description: String? = null
)

