package uk.gov.justice.digital.hmpps.model

import jakarta.validation.constraints.NotEmpty
import java.util.*

data class DeleteAppointmentsRequest(@NotEmpty val appointments: List<AppointmentReference>)
data class AppointmentReference(val reference: UUID)