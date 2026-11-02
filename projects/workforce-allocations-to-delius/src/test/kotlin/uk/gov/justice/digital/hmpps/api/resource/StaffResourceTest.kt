package uk.gov.justice.digital.hmpps.api.resource

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.service.StaffService

@ExtendWith(MockitoExtension::class)
class StaffResourceTest {
    @Mock
    lateinit var staffService: StaffService

    @InjectMocks
    lateinit var staffResource: StaffResource

    @Test
    fun `details resource calls service`() {
        staffResource.officerView("ABC")
        verify(staffService).getOfficerView("ABC")
    }

    @Test
    fun `active cases calls service`() {
        staffResource.activeCases("ABC", teamCode = null, listOf(PersonGenerator.DEFAULT.crn))
        verify(staffService).getActiveCases("ABC", listOf(PersonGenerator.DEFAULT.crn))
    }

    @Test
    fun `active cases calls service with team code filter`() {
        staffResource.activeCases("ABC", "TEAM1", listOf(PersonGenerator.DEFAULT.crn))
        verify(staffService).getActiveCases("ABC", listOf(PersonGenerator.DEFAULT.crn), "TEAM1")
    }

    @Test
    fun `get active cases calls service`() {
        staffResource.getActiveCases("ABC")
        verify(staffService).getActiveCases("ABC")
    }

    @Test
    fun `get active cases calls service with team code filter`() {
        staffResource.getActiveCases("ABC", "TEAM1")
        verify(staffService).getActiveCases("ABC", "TEAM1")
    }
}
