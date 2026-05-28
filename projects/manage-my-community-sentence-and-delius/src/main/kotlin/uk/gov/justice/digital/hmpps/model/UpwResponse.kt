package uk.gov.justice.digital.hmpps.model

import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.model.OfficeAddress.Companion.toModel
import uk.gov.justice.digital.hmpps.model.UpwProject.Companion.toModel

data class UpwResponse(
    val pickUpLocation: OfficeAddress? = null,
    val project: UpwProject,
) {
    companion object {
        fun UnpaidWorkAppointment.toResponse() = UpwResponse(
            pickUpLocation = pickUpLocation?.toModel(),
            project = project.toModel(),
        )
    }
}