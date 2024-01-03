package uk.gov.justice.digital.hmpps.api.resource

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator
import uk.gov.justice.digital.hmpps.service.OfficeAddressService

@ExtendWith(MockitoExtension::class)
internal class OfficeResourceTest {

    @Mock
    lateinit var officeAddressService: OfficeAddressService

    @InjectMocks
    lateinit var resource: OfficeResource

    @Test
    fun `calls office address service`() {
        val address = OfficeLocationGenerator.generateOfficeAddress(OfficeLocationGenerator.LOCATION_BRK_2, OfficeLocationGenerator.DISTRICT_BRK)
        whenever(officeAddressService.findAddresses("berk", "Read", PageRequest.of(0, 1))).thenReturn(PageImpl(listOf(address)))
        val res = resource.findAddresses("berk", "Read", 0, 1)
        assertThat(res.results[0].officeName, equalTo("Reading Office"))
    }
}
