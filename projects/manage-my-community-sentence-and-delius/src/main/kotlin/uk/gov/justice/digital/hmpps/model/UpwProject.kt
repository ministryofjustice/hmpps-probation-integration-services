package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProject
import uk.gov.justice.digital.hmpps.model.Address.Companion.toModel

data class UpwProject(
    val code: String,
    val description: String,
    val address: OfficeAddress? = null,
) {
    companion object {
        fun UnpaidWorkProject.toModel() = UpwProject(
            code = code,
            description = name,
            address = placementAddress?.toModel()
        )
    }
}