package uk.gov.justice.digital.hmpps.api.resource

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.service.AllocationDemandService

@ExtendWith(MockitoExtension::class)
class AllocationDemandResourceTest {
    @Mock lateinit var allocationDemandService: AllocationDemandService
    @InjectMocks lateinit var allocationDemandResource: AllocationDemandResource

    @Test
    fun `find unallocated handles empty request`() {
        val response = allocationDemandResource.findUnallocatedForTeam(AllocationDemandRequest(emptyList()))
        assertThat(response.cases, empty())
    }

    @Test
    fun `choose practitioner resource calls service`() {
        allocationDemandResource.choosePractitioner("ABC")
        verify(allocationDemandService).getChoosePractitionerResponse("ABC", listOf())
    }
}
