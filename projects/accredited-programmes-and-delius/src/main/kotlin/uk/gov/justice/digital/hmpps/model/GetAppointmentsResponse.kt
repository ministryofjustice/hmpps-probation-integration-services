package uk.gov.justice.digital.hmpps.model

data class GetAppointmentsResponse(val content: Map<String, List<AppointmentResponse>>)