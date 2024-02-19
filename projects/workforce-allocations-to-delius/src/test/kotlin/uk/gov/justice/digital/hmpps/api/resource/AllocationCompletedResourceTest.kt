package uk.gov.justice.digital.hmpps.api.resource

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.service.AllocationCompletedService

@ExtendWith(MockitoExtension::class)
class AllocationCompletedResourceTest {
    @Mock
    lateinit var allocationCompletedService: AllocationCompletedService

    @InjectMocks
    lateinit var allocationCompletedResource: AllocationCompletedResource

    @Test
    fun `details resource calls service`() {
        allocationCompletedResource.details("ABC", "123", "DEF")
        verify(allocationCompletedService).getDetails("ABC", "123", "DEF")
    }
}
