package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.entity.staff.ProbationAreaUserRepository
import uk.gov.justice.digital.hmpps.entity.staff.TeamRepository
import uk.gov.justice.digital.hmpps.entity.staff.UserRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class ProvidersServiceTest {
    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var probationAreaUserRepository: ProbationAreaUserRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository

    @InjectMocks
    lateinit var providersService: ProvidersService

    @Test
    fun `getSessions startDate and endDate must be within 7 days`() {
        val exception = assertThrows<IllegalArgumentException> {
            providersService.getSessions("N01UPW", LocalDate.now().minusDays(8), LocalDate.now())
        }

        assertThat(exception.message).isEqualTo("Date range cannot be greater than 7 days")
    }
}