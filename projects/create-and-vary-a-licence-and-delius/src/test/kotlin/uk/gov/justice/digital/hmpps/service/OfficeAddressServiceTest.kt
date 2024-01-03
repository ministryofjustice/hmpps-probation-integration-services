package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.OfficeLocationRepository

@ExtendWith(MockitoExtension::class)
internal class OfficeAddressServiceTest {

    @Mock
    lateinit var officeLocationRepository: OfficeLocationRepository

    @InjectMocks
    lateinit var service: OfficeAddressService

    @Test
    fun `calls office location repository`() {
        val location = OfficeLocationGenerator.generateLocation(
            OfficeLocationGenerator.LOCATION_BRK_1,
            OfficeLocationGenerator.DISTRICT_BRK
        )
        whenever(officeLocationRepository.findByLduAndOfficeName("berk", "Brack", Pageable.ofSize(1))).thenReturn(
            PageImpl(listOf(location))
        )
        val res = service.findAddresses("berk", "Brack", Pageable.ofSize(1))
        assertThat(res.content[0].officeName, equalTo("Bracknell Office"))
    }
}
