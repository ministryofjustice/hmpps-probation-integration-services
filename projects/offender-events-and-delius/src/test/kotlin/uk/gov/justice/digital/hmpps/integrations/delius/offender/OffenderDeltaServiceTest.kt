package uk.gov.justice.digital.hmpps.integrations.delius.offender

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.tier.ManagementTierEvent
import uk.gov.justice.digital.hmpps.integrations.delius.tier.ManagementTierEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.tier.ReferenceData
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class OffenderDeltaServiceTest {

    @Mock
    private lateinit var repository: OffenderDeltaRepository

    @Mock
    private lateinit var managementTierEventRepository: ManagementTierEventRepository

    @Mock
    private lateinit var publisher: NotificationPublisher

    private lateinit var service: OffenderDeltaService

    @BeforeEach
    fun setup() {
        service = OffenderDeltaService(50, repository, managementTierEventRepository, publisher)
    }

    @ParameterizedTest
    @MethodSource("deltas")
    fun `counts are correctly reported`(deltas: List<OffenderDelta>, expectedCounts: Pair<Int, Int>) {
        whenever(repository.findAll(any<Pageable>())).thenReturn(PageImpl(deltas))
        val counts = service.checkAndSendEvents()

        assertThat(expectedCounts, equalTo(counts))
    }

    @Test
    fun `do not publish registration management tier events`() {
        val mteId = 1L
        whenever(managementTierEventRepository.findById(mteId))
            .thenReturn(Optional.of(ManagementTierEvent(mteId, ReferenceData("REG", 1L))))
        val deltas = listOf(OffenderDeltaGenerator.generate(sourceId = mteId, sourceTable = "MANAGEMENT_TIER_EVENT"))
        whenever(repository.findAll(any<Pageable>())).thenReturn(PageImpl(deltas))
        val counts = service.checkAndSendEvents()
        assertThat(counts.first, equalTo(0))
        verify(publisher, never()).publish(any())
    }

    companion object {
        @JvmStatic
        private fun deltas() = listOf(
            Arguments.of(listOf(OffenderDeltaGenerator.generate()), Pair(2, 0)),
            Arguments.of(
                listOf(
                    OffenderDeltaGenerator.generate(offender = null),
                    OffenderDeltaGenerator.generate(sourceTable = "ALIAS")
                ),
                Pair(2, 1)
            ),
            Arguments.of(listOf(OffenderDeltaGenerator.generate(offender = null)), Pair(0, 1))
        )
    }
}
