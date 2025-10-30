package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import java.time.LocalDate
import java.time.LocalTime

object UPWGenerator {
    val DEFAULT_UPW_PROJECT = generateUpwProject(
        name = "Default UPW Project",
        code = "N01DEFAULT",
        teamId = TeamGenerator.DEFAULT_UPW_TEAM.id
    )
    val SECOND_UPW_PROJECT = generateUpwProject(
        name = "Second UPW Project",
        code = "N01SECOND",
        teamId = TeamGenerator.DEFAULT_UPW_TEAM.id
    )

    val DEFAULT_UPW_PROJECT_AVAILABILITY = generateUpwProjectAvailability(
        upwProjectId = DEFAULT_UPW_PROJECT.id
    )
    val SECOND_UPW_PROJECT_AVAILABILITY = generateUpwProjectAvailability(
        upwProjectId = SECOND_UPW_PROJECT.id
    )

    val DEFAULT_DISPOSAL = generateDisposal()
    val SECOND_DISPOSAL = generateDisposal()

    val DEFAULT_UPW_DETAILS = generateUpwDetails(disposalId = DEFAULT_DISPOSAL.id)
    val SECOND_UPW_DETAILS = generateUpwDetails(disposalId = SECOND_DISPOSAL.id)
    val THIRD_UPW_DETAILS = generateUpwDetails(disposalId = SECOND_DISPOSAL.id)


    val DEFAULT_CONTACT = generateContact(latestEnforcementActionId = ReferenceDataGenerator.DEFAULT_ENFORCEMENT_ACTION.id)
    val CONTACT_NO_ENFORCEMENT = generateContact(latestEnforcementActionId = null)

    val DEFAULT_UPW_APPOINTMENT = generateUpwAppointment(
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(17, 0),
        appointmentDate = LocalDate.now(),
        upwProjectId = DEFAULT_UPW_PROJECT.id,
        upwDetailsId = DEFAULT_UPW_DETAILS.id,
        contactId = DEFAULT_CONTACT.id,
        contactOutcomeTypeId = 1L
    )

    val UPW_APPOINTMENT_NO_ENFORCEMENT = generateUpwAppointment(
        startTime = LocalTime.of(10, 15),
        endTime = LocalTime.of(16, 30),
        appointmentDate = LocalDate.now(),
        upwProjectId = DEFAULT_UPW_PROJECT.id,
        upwDetailsId = DEFAULT_UPW_DETAILS.id,
        contactId = CONTACT_NO_ENFORCEMENT.id,
        contactOutcomeTypeId = 1L
    )

    val UPW_APPOINTMENT_NO_OUTCOME = generateUpwAppointment(
        startTime = LocalTime.of(12, 0),
        endTime = LocalTime.of(14, 0),
        appointmentDate = LocalDate.now().plusDays(1),
        upwProjectId = SECOND_UPW_PROJECT.id,
        upwDetailsId = SECOND_UPW_DETAILS.id,
        contactId = CONTACT_NO_ENFORCEMENT.id,
        contactOutcomeTypeId = null
    )

    val SECOND_UPW_APPOINTMENT_OUTCOME_NO_ENFORCEMENT = generateUpwAppointment(
        startTime = LocalTime.of(12, 0),
        endTime = LocalTime.of(14, 0),
        appointmentDate = LocalDate.now().plusDays(1),
        upwProjectId = SECOND_UPW_PROJECT.id,
        upwDetailsId = THIRD_UPW_DETAILS.id,
        contactId = CONTACT_NO_ENFORCEMENT.id,
        contactOutcomeTypeId = 1L
    )

    fun generateUpwProject(
        id: Long = IdGenerator.getAndIncrement(),
        name: String,
        code: String,
        teamId: Long
    ) = UpwProject(id, name, code, teamId)

    fun generateUpwProjectAvailability(
        id: Long = IdGenerator.getAndIncrement(),
        upwProjectId: Long
    ) = UpwProjectAvailability(id, upwProjectId)

    fun generateDisposal(
        id: Long = IdGenerator.getAndIncrement(),
        softDeleted: Boolean = false
    ) = Disposal(id, softDeleted)

    fun generateUpwDetails(
        id: Long = IdGenerator.getAndIncrement(),
        disposalId: Long
    ) = UpwDetails(id, disposalId)

    fun generateContact(
        id: Long = IdGenerator.getAndIncrement(),
        latestEnforcementActionId: Long?
    ) = Contact(id, latestEnforcementActionId)

    fun generateUpwAppointment(
        id: Long = IdGenerator.getAndIncrement(),
        attended: String = "Y",
        complied: String = "Y",
        softDeleted: Boolean = false,
        startTime: LocalTime,
        endTime: LocalTime,
        appointmentDate: LocalDate,
        upwProjectId: Long,
        upwDetailsId: Long,
        contactId: Long,
        contactOutcomeTypeId: Long?
    ) = UpwAppointment(id, attended, complied, softDeleted, startTime, endTime, appointmentDate,
        upwProjectId, upwDetailsId, contactId, contactOutcomeTypeId)
}