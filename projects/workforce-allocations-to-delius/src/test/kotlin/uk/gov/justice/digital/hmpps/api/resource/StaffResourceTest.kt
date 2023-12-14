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
        staffResource.activeCases("ABC", listOf(PersonGenerator.DEFAULT.crn))
        verify(staffService).getActiveCases("ABC", listOf(PersonGenerator.DEFAULT.crn))
    }
}
