package uk.gov.justice.digital.hmpps.integrations.delius.offender

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import uk.gov.justice.digital.hmpps.data.generator.OffenderDeltaGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.tier.ManagementTierEventRepository
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

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
        service = OffenderDeltaService(50, repository, publisher)
    }

    @ParameterizedTest
    @MethodSource("deltas")
    fun `counts are correctly reported`(deltas: List<OffenderDelta>, expectedCounts: Pair<Int, Int>) {
        whenever(repository.findAll(any<Pageable>())).thenReturn(PageImpl(deltas))
        val counts = service.checkAndSendEvents()

        assertThat(expectedCounts, equalTo(counts))
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
