package uk.gov.justice.digital.hmpps.appointments.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.appointments.audit.BusinessInteractionCode
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.EnforcementAction
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.OfficeLocation
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Outcome
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Staff
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Team
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type
import uk.gov.justice.digital.hmpps.appointments.model.UpdateAppointment
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.LocationRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.OutcomeRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.StaffRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.TeamRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentReferenceDataRepositories.TypeRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.AppointmentRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.PersonRepository
import uk.gov.justice.digital.hmpps.appointments.test.TestData
import uk.gov.justice.digital.hmpps.appointments.test.TestData.FTC_OUTCOME
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class AppointmentServiceTest {
    @Mock
    private lateinit var appointmentRepository: AppointmentRepository

    @Mock
    private lateinit var personRepository: PersonRepository

    @Mock
    private lateinit var teamRepository: TeamRepository

    @Mock
    private lateinit var staffRepository: StaffRepository

    @Mock
    private lateinit var locationRepository: LocationRepository

    @Mock
    private lateinit var typeRepository: TypeRepository

    @Mock
    private lateinit var enforcementActionRepository: EnforcementActionRepository

    @Mock
    private lateinit var outcomeRepository: OutcomeRepository

    @Mock
    private lateinit var enforcementService: EnforcementService

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @InjectMocks
    private lateinit var appointmentService: AppointmentService

    @Test
    fun `attempt to create a past appointment without an outcome`() {
        assertThatThrownBy { TestData.createAppointment(date = LocalDate.now().minusDays(1), outcomeCode = null) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Outcome must be provided when creating an appointment in the past")
    }

    @Test
    fun `attempt to create appointment with invalid type`() {
        val request = TestData.createAppointment().copy(typeCode = "INVALID")
        assertThatThrownBy { appointmentService.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid Type codes: [INVALID]")
    }

    @Test
    fun `attempt to create appointment with invalid outcome`() {
        whenever(typeRepository.findAllByCodeIn(any())).thenReturn(listOf(TestData.TYPE))

        val request = TestData.createAppointment().copy(outcomeCode = "INVALID")
        assertThatThrownBy { appointmentService.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid Outcome codes: [INVALID]")
    }

    @Test
    fun `attempt to create appointment with invalid location`() {
        whenever(typeRepository.findAllByCodeIn(any())).thenReturn(listOf(TestData.TYPE))
        mockEnforcementReferenceData()

        val request = TestData.createAppointment().copy(locationCode = "INVALID")
        assertThatThrownBy { appointmentService.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid OfficeLocation codes: [INVALID]")
    }

    @Test
    fun `attempt to create appointment with invalid team`() {
        whenever(typeRepository.findAllByCodeIn(any())).thenReturn(listOf(TestData.TYPE))
        whenever(outcomeRepository.findAllByCodeIn(any())).thenReturn(emptyList())
        mockEnforcementReferenceData()
        whenever(locationRepository.findAllByCodeIn(any())).thenReturn(listOf(TestData.OFFICE_LOCATION))

        val request = TestData.createAppointment().copy(teamCode = "INVALID")
        assertThatThrownBy { appointmentService.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid Team codes: [INVALID]")
    }

    @Test
    fun `attempt to create appointment with invalid staff`() {
        mockCreateReferenceData()

        val request = TestData.createAppointment().copy(staffCode = "INVALID")
        assertThatThrownBy { appointmentService.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid Staff codes: [INVALID]")
    }

    @Test
    fun `attempt to create a scheduling conflict`() {
        mockCreateReferenceData()

        val request = TestData.createAppointment()
        whenever(appointmentRepository.schedulingConflictExists(any())).thenReturn(true)
        assertThatThrownBy { appointmentService.create(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Appointment with reference REF01 conflicts with an existing appointment")
    }

    @Test
    fun `create appointment successful`() {
        mockCreateReferenceData()
        val request = TestData.createAppointment()
        val saved = TestData.appointment(externalReference = request.reference)
        whenever(appointmentRepository.saveAll(any<List<AppointmentContact>>())).thenReturn(listOf(saved))

        val result = appointmentService.create(request)

        assertThat(result.id).isEqualTo(saved.id)
        assertThat(result.reference).isEqualTo(saved.externalReference)
        assertThat(result.date).isEqualTo(saved.date)
        assertThat(result.typeCode).isEqualTo(TestData.TYPE.code)
        assertThat(result.relatedTo.personId).isEqualTo(TestData.PERSON.id)
        verify(appointmentRepository).saveAll(check<List<AppointmentContact>> {
            assertThat(it).hasSize(1)
            assertThat(it[0].date).isEqualTo(request.date)
            assertThat(it[0].startTime.toLocalTime()).isEqualTo(request.startTime)
            assertThat(it[0].personId).isEqualTo(request.relatedTo.personId)
            assertThat(it[0].type).isEqualTo(TestData.TYPE)
            assertThat(it[0].staff).isEqualTo(TestData.STAFF)
            assertThat(it[0].team).isEqualTo(TestData.TEAM)
            assertThat(it[0].provider).isEqualTo(TestData.PROVIDER)
            assertThat(it[0].officeLocation).isEqualTo(TestData.OFFICE_LOCATION)
        })
        verify(appointmentRepository).schedulingConflictExists(any())
        verify(auditedInteractionService).createAuditedInteraction(
            interactionCode = eq(BusinessInteractionCode.ADD_CONTACT),
            params = eq(
                AuditedInteraction.Parameters(
                    mutableMapOf(
                        "offenderId" to TestData.PERSON.id,
                        "contactId" to saved.id!!,
                    )
                )
            ),
            outcome = eq(AuditedInteraction.Outcome.SUCCESS),
            dateTime = any(),
            username = anyOrNull()
        )
    }

    @Test
    fun `create appointment allowing schedule conflict`() {
        mockCreateReferenceData()
        val request = TestData.createAppointment().copy(allowConflicts = true)
        val saved = TestData.appointment(externalReference = request.reference)
        whenever(appointmentRepository.saveAll(any<List<AppointmentContact>>())).thenReturn(listOf(saved))

        val result = appointmentService.create(request)

        assertThat(result.id).isEqualTo(saved.id)
        assertThat(result.reference).isEqualTo(saved.externalReference)
        assertThat(result.date).isEqualTo(saved.date)
        assertThat(result.typeCode).isEqualTo(TestData.TYPE.code)
        assertThat(result.relatedTo.personId).isEqualTo(TestData.PERSON.id)
        verify(appointmentRepository, never()).schedulingConflictExists(any())
    }

    @Test
    fun `attempt to reschedule appointment with an existing outcome`() {
        val existing = TestData.appointment(outcome = TestData.OUTCOME)
        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))

        assertThatThrownBy {
            appointmentService.update(listOf(existing)) {
                reference = { existing.externalReference }
                amendDateTime = {
                    UpdateAppointment.Schedule(
                        date = LocalDate.now().plusDays(2),
                        startTime = LocalTime.NOON,
                        endTime = LocalTime.NOON.plusHours(1)
                    )
                }
            }
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Appointment with outcome cannot be rescheduled")

        assertThatThrownBy {
            appointmentService.update(listOf(existing)) {
                reference = { existing.externalReference }
                recreate = {
                    UpdateAppointment.RecreateAppointment(
                        date = LocalDate.now().plusDays(2),
                        startTime = LocalTime.NOON,
                        endTime = LocalTime.NOON.plusHours(1)
                    )
                }
            }
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Appointment with outcome cannot be rescheduled")
    }

    @Test
    fun `attempt to amend date and time of past appointment`() {
        val existing = TestData.appointment(date = LocalDate.now().minusDays(1))
        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))

        assertThatThrownBy {
            appointmentService.update(listOf(existing)) {
                reference = { existing.externalReference }
                amendDateTime = {
                    UpdateAppointment.Schedule(
                        date = LocalDate.now().minusDays(2),
                        startTime = LocalTime.NOON,
                        endTime = LocalTime.NOON.plusHours(1)
                    )
                }
            }
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Appointment must be in the future to amend the date and time")
    }

    @Test
    fun `attempt to amend date and time of appointment into the past`() {
        val existing = TestData.appointment(date = LocalDate.now().plusDays(1))
        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))

        assertThatThrownBy {
            appointmentService.update(listOf(existing)) {
                reference = { existing.externalReference }
                amendDateTime = {
                    UpdateAppointment.Schedule(
                        date = LocalDate.now().minusDays(2),
                        startTime = LocalTime.NOON,
                        endTime = LocalTime.NOON.plusHours(1)
                    )
                }
            }
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Appointment cannot be rescheduled into the past")
    }

    @Test
    fun `update appointment amend date time in future`() {
        val existing = TestData.appointment(date = LocalDate.now().plusDays(1))

        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))
        whenever(appointmentRepository.schedulingConflictExists(any())).thenReturn(false)

        val result = appointmentService.update(listOf(existing)) {
            reference = { existing.externalReference }
            amendDateTime = {
                UpdateAppointment.Schedule(
                    date = LocalDate.now().plusDays(2),
                    startTime = LocalTime.NOON,
                    endTime = LocalTime.NOON.plusHours(1)
                )
            }
        }

        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].date).isEqualTo(LocalDate.now().plusDays(2))
        assertThat(result[0].startTime).isEqualTo(LocalTime.NOON)
        assertThat(result[0].endTime).isEqualTo(LocalTime.NOON.plusHours(1))

        verify(auditedInteractionService).createAuditedInteraction(
            interactionCode = eq(BusinessInteractionCode.UPDATE_CONTACT),
            params = eq(
                AuditedInteraction.Parameters(
                    mutableMapOf(
                        "offenderId" to TestData.PERSON.id,
                        "contactId" to result[0].id,
                    )
                )
            ),
            outcome = eq(AuditedInteraction.Outcome.SUCCESS),
            dateTime = any(),
            username = anyOrNull()
        )
    }

    @Test
    fun `update appointment apply outcome`() {
        val existing = TestData.appointment()

        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))
        whenever(outcomeRepository.findAllByCodeIn(setOf(FTC_OUTCOME.code))).thenReturn(listOf(FTC_OUTCOME))
        mockEnforcementReferenceData()

        appointmentService.update(listOf(existing)) {
            reference = { existing.externalReference }
            applyOutcome = { UpdateAppointment.Outcome(FTC_OUTCOME.code) }
        }

        assertThat(existing.outcome?.code).isEqualTo(FTC_OUTCOME.code)
        assertThat(existing.attended).isEqualTo(FTC_OUTCOME.attended)
        assertThat(existing.complied).isEqualTo(FTC_OUTCOME.complied)
        verify(enforcementService).applyEnforcementAction(eq(existing), eq(TestData.ACTION), eq(TestData.REVIEW_TYPE))
    }

    @Test
    fun `update appointment reassign`() {
        val existing = TestData.appointment()
        val newStaff = Staff(id(), "NEW_STAFF")
        val newTeam = Team(id(), "NEW_TEAM", "New Team", TestData.PROVIDER)
        val newLocation = OfficeLocation(id(), "NEW_LOC")

        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))
        whenever(staffRepository.findAllByCodeIn(setOf(newStaff.code))).thenReturn(listOf(newStaff))
        whenever(teamRepository.findAllByCodeIn(setOf(newTeam.code))).thenReturn(listOf(newTeam))
        whenever(locationRepository.findAllByCodeIn(setOf(newLocation.code))).thenReturn(listOf(newLocation))

        appointmentService.update(listOf(existing)) {
            reference = { existing.externalReference }
            reassign = { UpdateAppointment.Assignee(newStaff.code, newTeam.code, newLocation.code) }
        }

        assertThat(existing.staff.code).isEqualTo(newStaff.code)
        assertThat(existing.team.code).isEqualTo(newTeam.code)
        assertThat(existing.officeLocation?.code).isEqualTo(newLocation.code)
    }

    @Test
    fun `update appointment append notes`() {
        val existing = TestData.appointment(notes = "Existing notes")

        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))

        appointmentService.update(listOf(existing)) {
            reference = { existing.externalReference }
            appendNotes = { "New notes" }
        }

        assertThat(existing.notes).isEqualTo("Existing notes\n\nNew notes")
    }

    @Test
    fun `update appointment flag as sensitive`() {
        val existing = TestData.appointment(sensitive = false)

        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))

        appointmentService.update(listOf(existing)) {
            reference = { existing.externalReference }
            flagAs = { UpdateAppointment.Flags(sensitive = true) }
        }

        assertThat(existing.sensitive).isEqualTo(true)
    }

    @Test
    fun `create multiple appointments`() {
        mockCreateReferenceData()
        val requests = listOf(
            TestData.createAppointment(reference = "REF01"),
            TestData.createAppointment(reference = "REF02")
        )
        val saved = requests.map { request -> TestData.appointment(externalReference = request.reference) }
        whenever(appointmentRepository.saveAll(any<List<AppointmentContact>>())).thenReturn(saved)

        val result = appointmentService.create(requests)

        assertThat(result.size).isEqualTo(2)
        assertThat(result[0].reference).isEqualTo("REF01")
        assertThat(result[1].reference).isEqualTo("REF02")
    }

    @Test
    fun `update appointment reschedule (amend)`() {
        // Appointment is in the future, so it should amend
        val existing = TestData.appointment(date = LocalDate.now().plusDays(1))

        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))

        appointmentService.update(listOf(existing)) {
            reference = { existing.externalReference }
            reschedule = {
                UpdateAppointment.RecreateAppointment(
                    date = LocalDate.now().plusDays(2),
                    startTime = LocalTime.NOON,
                    endTime = LocalTime.NOON.plusHours(1),
                    newReference = "NEW_REF"
                )
            }
        }

        assertThat(existing.date).isEqualTo(LocalDate.now().plusDays(2))
        verify(appointmentRepository, never()).saveAll(any<List<AppointmentContact>>())
    }

    @Test
    fun `update appointment reschedule (recreate)`() {
        mockCreateReferenceData()
        // Appointment is in the past, so it should recreate
        val existing = TestData.appointment(date = LocalDate.now().minusDays(1))
        val outcome = Outcome(id(), "RSSR", "Rescheduled", attended = false, complied = true, enforceable = false)

        whenever(appointmentRepository.findByExternalReferenceIn(listOf(existing.externalReference!!)))
            .thenReturn(listOf(existing))
        whenever(outcomeRepository.findAllByCodeIn(setOf("RSSR"))).thenReturn(listOf(outcome))
        whenever(appointmentRepository.saveAll(any<List<AppointmentContact>>())).thenAnswer {
            (it.arguments[0] as List<*>).map { contact ->
                TestData.appointment(externalReference = (contact as AppointmentContact).externalReference)
            }
        }

        appointmentService.update(listOf(existing)) {
            reference = { existing.externalReference }
            reschedule = {
                UpdateAppointment.RecreateAppointment(
                    date = LocalDate.now().plusDays(1),
                    startTime = LocalTime.NOON,
                    endTime = LocalTime.NOON.plusHours(1),
                    newReference = "NEW_REF",
                    rescheduledBy = UpdateAppointment.RecreateAppointment.RescheduledBy.PROBATION_SERVICE
                )
            }
        }

        assertThat(existing.externalReference).isEqualTo("REF01")
        assertThat(existing.date).isEqualTo(LocalDate.now().minusDays(1))
        assertThat(existing.outcome).isEqualTo(outcome)

        verify(appointmentRepository).saveAll(check<List<AppointmentContact>> { new ->
            assertThat(new).hasSize(1)
            assertThat(new[0].externalReference).isEqualTo("NEW_REF")
            assertThat(new[0].date).isEqualTo(LocalDate.now().plusDays(1))
            assertThat(new[0].outcome).isEqualTo(null)
        })
    }

    private fun mockCreateReferenceData() {
        whenever(typeRepository.findAllByCodeIn(any())).thenReturn(listOf(TestData.TYPE))
        whenever(outcomeRepository.findAllByCodeIn(any())).thenReturn(emptyList())
        whenever(locationRepository.findAllByCodeIn(any())).thenReturn(listOf(TestData.OFFICE_LOCATION))
        whenever(teamRepository.findAllByCodeIn(any())).thenReturn(listOf(TestData.TEAM))
        whenever(staffRepository.findAllByCodeIn(any())).thenReturn(listOf(TestData.STAFF))
        mockEnforcementReferenceData()
    }

    private fun mockEnforcementReferenceData() {
        whenever(enforcementActionRepository.findByCode(EnforcementAction.REFER_TO_PERSON_MANAGER))
            .thenReturn(TestData.ACTION)
        whenever(typeRepository.findByCode(Type.REVIEW_ENFORCEMENT_STATUS)).thenReturn(TestData.REVIEW_TYPE)
    }
}
