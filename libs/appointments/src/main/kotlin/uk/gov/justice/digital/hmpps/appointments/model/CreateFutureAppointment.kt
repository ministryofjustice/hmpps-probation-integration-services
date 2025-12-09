package uk.gov.justice.digital.hmpps.appointments.model

data class CreateFutureAppointment(
    override val externalReference: String,
    override val type: RequestCode,
    override val schedule: Schedule,
    override val relatedTo: ReferencedEntities,
    override val assigned: Assigned?,
    override val notes: String?,
    override val flagAs: FlagAs,
    val username: String? = null,
) : AppointmentRequest