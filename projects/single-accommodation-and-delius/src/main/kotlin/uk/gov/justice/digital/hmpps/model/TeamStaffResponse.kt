package uk.gov.justice.digital.hmpps.model

import org.springframework.data.web.PagedModel

data class TeamStaffResponse(
    val staff: List<TeamStaffMember>,
    val page: PagedModel.PageMetadata,
)

data class TeamStaffMember(
    val username: String?,
    val code: String,
    val name: Name,
)
