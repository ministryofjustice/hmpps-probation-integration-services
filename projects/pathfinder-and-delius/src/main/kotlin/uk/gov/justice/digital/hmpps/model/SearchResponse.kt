package uk.gov.justice.digital.hmpps.model

import java.time.LocalDate

// Using probation offender search model as fields used in csv reports so need to match existing schema
data class OffenderDetail(
    val firstName: String? = null,
    val middleNames: List<String>? = emptyList(),
    val surname: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: String? = null,
    val otherIds: IDs,
    val offenderProfile: OffenderProfile? = null,
    val offenderAliases: List<OffenderAlias>? = null,
    val offenderManagers: List<OffenderManager>? = null,
    val mappa: MappaDetails? = null,
)

data class IDs(
    val crn: String,
    val nomsNumber: String? = null,
    val pncNumber: String? = null,
)

data class OffenderProfile(
    val ethnicity: String? = null,
    val nationality: String? = null,
    val religion: String? = null,
)

data class OffenderManager(
    val staff: StaffHuman? = null,
    val team: SearchResponseTeam? = null,
    val probationArea: ProbationArea? = null,
    val active: Boolean? = null,
)

data class StaffHuman(
    val code: String? = null,
    val forenames: String? = null,
    val surname: String? = null,
    val unallocated: Boolean? = null,
)

data class SearchResponseTeam(
    val code: String? = null,
    val description: String? = null,
    val district: KeyValue? = null,
)

data class KeyValue(
    val code: String? = null,
    val description: String? = null,
)

data class MappaDetails(
    val level: Int? = null,
    val levelDescription: String? = null,
    val category: Int? = null,
    val categoryDescription: String? = null,
    val startDate: LocalDate? = null,
    val reviewDate: LocalDate? = null,
    val team: KeyValue? = null,
    val officer: StaffHuman? = null,
    val probationArea: KeyValue? = null,
    val notes: String? = null,
)