package uk.gov.justice.digital.hmpps.service


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RegistrationRepository

@ExtendWith(MockitoExtension::class)
class MappaCategoryResolverServiceTest {
    @Mock
    lateinit var registrationRepository: RegistrationRepository

    @InjectMocks
    lateinit var service: MappaCategoryResolverService

    @Test
    fun `registration with Mapp M1 returns 1`() {
        whenever( registrationRepository.findByMappaCategoryByPersonId(1L) ).thenReturn(1)
        var actual = service.resolveMappaCategory(1L)
        assertThat(actual).isEqualTo(1)
    }

    @Test
    fun `registration with no Mapp returns 0`() {
        whenever( registrationRepository.findByMappaCategoryByPersonId(1L) ).thenReturn(0)
        var actual = service.resolveMappaCategory(1L)
        assertThat(actual).isEqualTo(0)
    }
}