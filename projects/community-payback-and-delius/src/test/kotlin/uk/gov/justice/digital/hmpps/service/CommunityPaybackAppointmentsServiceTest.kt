package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkProjectRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwMinutesDto
import java.time.LocalTime

class CommunityPaybackAppointmentsServiceTest {
    private val projectRepository = mock<UnpaidWorkProjectRepository>()
    private val appointmentRepository = mock<UnpaidWorkAppointmentRepository>()
    private val userAccessService = mock<UserAccessService>()

    private val service = CommunityPaybackAppointmentsService(
        unpaidWorkProjectRepository = projectRepository,
        unpaidWorkAppointmentRepository = appointmentRepository,
        createUnpaidWorkAppointmentRepository = mock(),
        contactOutcomeRepository = mock(),
        referenceDataRepository = mock(),
        staffRepository = mock(),
        userAccessService = userAccessService,
        appointmentService = mock(),
        eventRepository = mock(),
        upwDetailsRepository = mock(),
        officeLocationRepository = mock(),
        teamRepository = mock(),
    )

    @Test
    fun `appointment responses have no last updated username when user is null`() {
        val project = UPWGenerator.UPW_PROJECT_1
        val appointment = UPWGenerator.generateUpwAppointment(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            date = UPWGenerator.DEFAULT_UPW_APPOINTMENT.date,
            project = project,
            details = UPWGenerator.UPW_DETAILS_1,
            contact = UPWGenerator.DEFAULT_CONTACT,
            pickupLocation = null,
            pickupTime = null,
            penaltyTime = null,
            person = PersonGenerator.DEFAULT_PERSON,
            staff = StaffGenerator.DEFAULT_STAFF,
            team = TeamGenerator.DEFAULT_UPW_TEAM,
        )
        val username = "DefaultUser"
        val crn = appointment.person.crn
        val access = CaseAccess(crn, userExcluded = false, userRestricted = false)
        val pageable = PageRequest.of(0, 10)
        val detailsIds = listOf(appointment.details.id)
        val minutes = object : UpwMinutesDto {
            override val id = appointment.details.id
            override val requiredMinutes = 120L
            override val completedMinutes = 30L
            override val positiveAdjustments = 0L
            override val negativeAdjustments = 0L
        }

        assertThat(appointment.lastUpdatedByUser).isNull()
        whenever(projectRepository.findByCode(project.code)).thenReturn(project)
        whenever(appointmentRepository.getUpwAppointmentById(appointment.id)).thenReturn(appointment)
        whenever(userAccessService.caseAccessFor(username, crn)).thenReturn(access)
        whenever(userAccessService.userAccessFor(username, listOf(crn))).thenReturn(UserAccess(listOf(access)))
        whenever(appointmentRepository.getUpwRequiredAndCompletedMinutes(detailsIds)).thenReturn(listOf(minutes))
        whenever(
            appointmentRepository.findByDateAndProjectCodeAndDetailsSoftDeletedFalse(appointment.date, project.code)
        ).thenReturn(listOf(appointment))
        whenever(
            appointmentRepository.findAppointments(
                null, null, null, null, null, null, null, listOf(appointment.id), null, pageable
            )
        ).thenReturn(PageImpl(listOf(appointment), pageable, 1))

        assertThat(service.getAppointment(project.code, appointment.id, username).lastUpdatedBy).isNull()

        val list = service.getAppointments(
            username = username,
            crn = null,
            eventNumber = null,
            fromDate = null,
            toDate = null,
            projectCodes = null,
            projectTypeCodes = null,
            outcomeCodes = null,
            appointmentIds = listOf(appointment.id),
            references = null,
            pageable = pageable,
        )
        assertThat(list.content.single().lastUpdatedBy).isNull()

        val session = service.getSession(project.code, appointment.date, username)
        assertThat(session.appointmentSummaries.single().lastUpdatedBy).isNull()
    }
}