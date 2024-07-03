package uk.gov.justice.digital.hmpps.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.api.model.conviction.Requirement
import java.time.LocalDate
import java.time.ZonedDateTime

data class NsiDetails (
    val nsis: List<Nsi>
)

data class Nsi(
    val nsiId: Long,
    val nsiType: KeyValue,
    val nsiSubType: KeyValue?,
    val nsiOutcome: KeyValue?,
    val requirement: Requirement? = null,
    val nsiStatus: KeyValue,
    val statusDateTime: ZonedDateTime,
    val actualStartDate: LocalDate?,
    val expectedStartDate: LocalDate?,
    val actualEndDate: LocalDate?,
    val expectedEndDate: LocalDate?,
    val referralDate: LocalDate,
    val length: Long?,
    val lengthUnit: String?,
    val nsiManagers: List<NsiManager>,
//    val notes: String,
//    val intendedProvider: ProbationArea,
//    val active: Boolean,
//    val softDeleted: Boolean,
//    val externalReference: String
)

data class NsiManager (
    val probationArea: ProbationArea,
    val team: Team,
    val staff: StaffDetails,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

data class StaffDetails (
    @Schema(description = "the optional username of this staff member, will be absent if the staff member is not a user of Delius", example = "SheilaHancockNPS")
    val username: String?,
    @Schema(description = "staff code AKA officer code", example = "SH00001")
    val staffCode: String,
    @Schema(description = "staff identifier", example = "123456")
    val staffIdentifier: Long,
    @Schema(description = "staff name details")
    val staff: Human,
    @Schema(description = "all teams related to this staff member")
    val teams: List<Team>,
    @Schema(description = "provider this staff member is associated with")
    val probationArea: ProbationArea,
    @Schema(description = "Staff Grade", example = "PO,CRC - PO")
    val staffGrade: KeyValue?
)