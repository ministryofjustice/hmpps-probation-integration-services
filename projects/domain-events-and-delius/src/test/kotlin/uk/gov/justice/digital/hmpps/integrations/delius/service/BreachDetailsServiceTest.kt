package uk.gov.justice.digital.hmpps.integrations.delius.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.model.BreachDetails
import uk.gov.justice.digital.hmpps.integrations.delius.repository.NsiRepository
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class BreachDetailsServiceTest {

    @Mock
    lateinit var nsiRepository: NsiRepository

    @InjectMocks
    lateinit var breachDetailsService: BreachDetailsService

    @Test
    fun `should return breach details`() {
        val nsiId = NsiGenerator.DEFAULT_NSI.id
        whenever(nsiRepository.findById(nsiId)).thenReturn(Optional.of(NsiGenerator.DEFAULT_NSI))

        val breachDetails = breachDetailsService.getBreachDetails(nsiId)

        val expectedBreachDetails = BreachDetails(
            ZonedDateTime.parse("2022-01-31T00:00:00+00:00"),
            "D006926",
            "1",
        )
        assertThat(breachDetails).isEqualTo(expectedBreachDetails)
    }

    @Test
    fun `should throw NotFoundException if NSI not found`() {
        val nsiId = NsiGenerator.DEFAULT_NSI.id
        whenever(nsiRepository.findById(nsiId)).thenReturn(Optional.empty())

        assertThrows<NotFoundException> { breachDetailsService.getBreachDetails(nsiId) }
    }
}
