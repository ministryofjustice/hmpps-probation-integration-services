package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class StaffServiceTest {
    @Mock lateinit var staffRepository: StaffRepository
    @Mock lateinit var ldapService: LdapService
    @InjectMocks lateinit var staffService: StaffService

    @Test
    fun `staff not found`() {
        val exception = assertThrows<NotFoundException> {
            staffService.getOfficerView("UNK")
        }
        assertThat(exception.message, equalTo("Staff with code of UNK not found"))
    }

    @Test
    fun `details response is mapped and returned`() {
        val staff = StaffGenerator.DEFAULT
        whenever(ldapService.findEmailForStaff(staff)).thenReturn("test@test.com")
        whenever(staffRepository.findByCode(staff.code)).thenReturn(staff)
        whenever(staffRepository.getParoleReportsDueCountByStaffId(staff.id, LocalDate.now().plusWeeks(4))).thenReturn(1L)
        whenever(staffRepository.getSentencesDueCountByStaffId(staff.id, LocalDate.now().plusWeeks(4))).thenReturn(2L)
        whenever(staffRepository.getKeyDateCountByCodeAndStaffId(staff.id, "EXP", LocalDate.now().plusWeeks(4))).thenReturn(3L)

        val response = staffService.getOfficerView(staff.code)

        assertThat(response.code, equalTo(staff.code))
        assertThat(response.name.forename, equalTo(staff.forename))
        assertThat(response.name.middleName, equalTo(staff.middleName))
        assertThat(response.name.surname, equalTo(staff.surname))
        assertThat(response.grade, equalTo("PSO"))
        assertThat(response.email, equalTo("test@test.com"))
        assertThat(response.paroleReportsToCompleteInNext4Weeks, equalTo(1L))
        assertThat(response.casesDueToEndInNext4Weeks, equalTo(2L))
        assertThat(response.releasesWithinNext4Weeks, equalTo(3L))
    }
}
