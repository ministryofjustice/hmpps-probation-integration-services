package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.generateOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcome
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ContactTypeOutcomeId

object AppointmentGenerator {

    val APPOINTMENT_TYPES = CreateAppointment.Type.entries.map { generateType(it.code, attendanceType = true) }

    fun generateType(
        code: String,
        description: String = "Description for $code",
        attendanceType: Boolean,
        id: Long = IdGenerator.getAndIncrement()
    ) = ContactType(IdGenerator.getAndIncrement(), code, true, description)

    val ATTENDED_COMPLIED = generateOutcome("ATTC", "Attended - Complied", true, true)

    val CONTACT_TYPE_OUTCOMES = APPOINTMENT_TYPES.map {
        generateContactTypeOutcome(it.id, ATTENDED_COMPLIED.id, it, ATTENDED_COMPLIED)
    }

    fun generateContactTypeOutcome(
        contactTypeId: Long,
        contactOutcomeTypeId: Long,
        contactType: ContactType,
        outcome: ContactOutcome
    ) = ContactTypeOutcome(
        ContactTypeOutcomeId(contactTypeId, contactOutcomeTypeId),
        contactType,
        outcome
    )
}
