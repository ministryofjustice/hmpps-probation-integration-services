package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.core.IsInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.integrations.randm.ReferAndMonitorClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.service.NsiService
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class ReferralEndSubmittedTest {

    @Mock
    lateinit var ramClient: ReferAndMonitorClient

    @Mock
    lateinit var nsiService: NsiService

    @InjectMocks
    lateinit var referralEnd: ReferralEndSubmitted

    @Test
    fun `unable to retrieve sent referral throws Not Found Exception`() {
        val event = HmppsDomainEvent(
            DomainEventType.ReferralEnded.name,
            1,
            "https://fake.org/url"
        )

        val res = referralEnd.referralEnded(event)
        assertThat(res, IsInstanceOf(EventProcessingResult.Failure::class.java))
        assertThat(res.properties, equalTo(mapOf("message" to "Unable to retrieve session: ${event.detailUrl}")))
    }

    @ParameterizedTest
    @MethodSource("sentReferralDates")
    fun `Sent Referral End Date Correctly Identified`(sr: SentReferral, expectedEndDate: ZonedDateTime) {
        assertThat(sr.endDate, equalTo(expectedEndDate))
    }

    companion object {
        private val endRequestedAt = ZonedDateTime.now().minusDays(1)
        private val concludedAt = ZonedDateTime.now()
        private val sentReferral = SentReferral(
            "Ref123",
            1,
            Referral(
                UUID.randomUUID().toString(),
                Provider("Special Provider"),
                "Contract Type",
                ZonedDateTime.now().minusDays(1)
            ),
            endRequestedAt,
            null
        )

        @JvmStatic
        fun sentReferralDates() = listOf(
            Arguments.of(sentReferral, endRequestedAt),
            Arguments.of(sentReferral.copy(concludedAt = concludedAt), concludedAt)
        )
    }
}
