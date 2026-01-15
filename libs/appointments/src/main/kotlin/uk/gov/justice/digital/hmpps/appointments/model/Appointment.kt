package uk.gov.justice.digital.hmpps.appointments.model

import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities
import java.time.LocalDate
import java.time.LocalTime

data class Appointment(
    val id: Long,
    val reference: String,
    val typeCode: String,
    val relatedTo: ReferencedEntities,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime?,
    val staffCode: String,
    val teamCode: String,
    val locationCode: String? = null,
    val outcomeCode: String? = null,
    val notes: String? = null,
    val sensitive: Boolean? = false,
    val exportToVisor: Boolean? = false,
) {
    internal constructor(entity: AppointmentEntities.AppointmentContact) : this(
        id = entity.id!!,
        reference = entity.externalReference!!,
        typeCode = entity.type.code,
        relatedTo = ReferencedEntities(
            crn = entity.person?.crn,
            personId = entity.personId,
            eventId = entity.eventId,
            nonStatutoryInterventionId = entity.nsiId,
            licenceConditionId = entity.licenceConditionId,
            requirementId = entity.requirementId,
            pssRequirementId = entity.pssRequirementId,
        ),
        date = entity.date,
        startTime = entity.startTime.toLocalTime(),
        endTime = entity.endTime?.toLocalTime(),
        staffCode = entity.staff.code,
        teamCode = entity.team.code,
        locationCode = entity.officeLocation?.code,
        outcomeCode = entity.outcome?.code,
        notes = entity.notes,
        sensitive = entity.sensitive,
        exportToVisor = entity.visorContact,
    )
}
