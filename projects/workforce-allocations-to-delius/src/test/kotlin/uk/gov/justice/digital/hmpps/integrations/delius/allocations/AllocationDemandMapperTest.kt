package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.api.allocationdemand.AllocationResponse
import uk.gov.justice.digital.hmpps.api.allocationdemand.CaseType
import uk.gov.justice.digital.hmpps.api.allocationdemand.Event
import uk.gov.justice.digital.hmpps.api.allocationdemand.InitialAppointment
import uk.gov.justice.digital.hmpps.api.allocationdemand.ManagementStatus
import uk.gov.justice.digital.hmpps.api.allocationdemand.Manager
import uk.gov.justice.digital.hmpps.api.allocationdemand.Name
import uk.gov.justice.digital.hmpps.api.allocationdemand.ProbationStatus
import uk.gov.justice.digital.hmpps.api.allocationdemand.Sentence
import java.sql.Date
import java.sql.ResultSet
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AllocationDemandMapperTest {

    @Mock
    lateinit var resultSet: ResultSet

    lateinit var expected: AllocationResponse

    @BeforeEach
    fun setUp() {
        expected = AllocationResponse(
            "X123456",
            Name("John", "William", "Smith"),
            Event(
                "1",
                Manager(
                    "EM123",
                    Name("Emma", "Jane", "Butane"),
                    "T123",
                    "PSO"
                )
            ),
            Sentence("Community Service", LocalDate.now().minusDays(10), "3 Months"),
            InitialAppointment(LocalDate.now().plusDays(10)),
            CaseType.COMMUNITY,
            ProbationStatus(
                ManagementStatus.PREVIOUSLY_MANAGED,
                Manager(
                    "MAN1",
                    Name("Bob", null, "Smith"), "Team1"
                )
            )
        )

        whenever(resultSet.getString("crn")).thenReturn(expected.crn)
        whenever(resultSet.getString("forename")).thenReturn(expected.name.forename)
        whenever(resultSet.getString("middle_name")).thenReturn(expected.name.middleName)
        whenever(resultSet.getString("surname")).thenReturn(expected.name.surname)
        whenever(resultSet.getString("event_number")).thenReturn(expected.event.number)
        whenever(resultSet.getString("staff_code")).thenReturn(expected.event.manager.code)
        whenever(resultSet.getString("staff_forename")).thenReturn(expected.event.manager.name.forename)
        whenever(resultSet.getString("staff_middle_name")).thenReturn(expected.event.manager.name.middleName)
        whenever(resultSet.getString("staff_surname")).thenReturn(expected.event.manager.name.surname)
        whenever(resultSet.getString("team_code")).thenReturn(expected.event.manager.teamCode)
        whenever(resultSet.getString("sentence_type")).thenReturn(expected.sentence?.type)
        whenever(resultSet.getDate("sentence_date")).thenReturn(Date.valueOf(expected.sentence?.date))
        whenever(resultSet.getString("sentence_length_value")).thenReturn("3")
        whenever(resultSet.getString("sentence_length_unit")).thenReturn("Months")
        whenever(resultSet.getDate("initial_appointment_date")).thenReturn(Date.valueOf(expected.initialAppointment?.date))
        whenever(resultSet.getString("case_type")).thenReturn("COMMUNITY")
        whenever(resultSet.getString("previous_staff_code")).thenReturn(expected.status.previousManager?.code)
        whenever(resultSet.getString("previous_staff_forename")).thenReturn(expected.status.previousManager?.name?.forename)
        whenever(resultSet.getString("previous_staff_middle_name")).thenReturn(expected.status.previousManager?.name?.middleName)
        whenever(resultSet.getString("previous_staff_surname")).thenReturn(expected.status.previousManager?.name?.surname)
        whenever(resultSet.getString("previous_team_code")).thenReturn(expected.status.previousManager?.teamCode)
        whenever(resultSet.getString("management_status")).thenReturn("PREVIOUSLY_MANAGED")
        whenever(resultSet.getString("order_manager_grade")).thenReturn("PSQ")
    }

    @Test
    fun `mapper correctly maps row results`() {
        val res = AllocationDemandRepository.mapper.mapRow(resultSet, 1)

        assertEquals(expected, res)
    }
}
