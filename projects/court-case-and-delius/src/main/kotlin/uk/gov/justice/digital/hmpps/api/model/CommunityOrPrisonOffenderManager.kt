package uk.gov.justice.digital.hmpps.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class CommunityOrPrisonOffenderManager(
    @Schema(description = "Staff code", example = "CHSE755")
    val staffCode: String?,
    @Schema(description = "Staff id", example = "123455")
    val staffId: Long?,
    @Schema(description = "True if this offender manager is the current responsible officer", example = "true")
    val isResponsibleOfficer: Boolean,
    @Schema(description = "True if this offender manager is the prison OM else False", example = "true")
    val isPrisonOffenderManager: Boolean,
    @Schema(
        description = "True if no real offender manager has been allocated and this is just a placeholder",
        example = "true"
    )
    val isUnallocated: Boolean,
    @Schema(description = "staff name and contact details")
    val staff: ContactableHuman?,
    @Schema(description = "Team details for this offender manager")
    val team: Team? = null,
    @Schema(description = "Probation area / prison institution for this OM")
    val probationArea: ProbationArea? = null,

    @Schema(description = "Date since the offender manager was assigned", example = "2019-12-04")
    val fromDate: LocalDate? = null,

    @Schema(description = "Grade details for this offender manager")
    val grade: KeyValue? = null
)

data class ContactableHuman(
    @Schema(description = "Given names", example = "Sheila Linda")
    val forenames: String,
    @Schema(description = "Family name", example = "Hancock")
    val surname: String,
    @Schema(description = "Email address", example = "officer@gov.uk")
    val email: String? = null,
    @Schema(description = "Phone number", example = "0123411278")
    val phoneNumber: String? = null
) {
    fun capitalise(): ContactableHuman = this.copy(
        forenames = this.forenames.uppercase(),
        surname = this.surname.uppercase()
    )
}
