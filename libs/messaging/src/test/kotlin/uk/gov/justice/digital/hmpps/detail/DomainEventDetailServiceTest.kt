package uk.gov.justice.digital.hmpps.detail

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.net.URI

@ExtendWith(MockitoExtension::class)
internal class DomainEventDetailServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var client: RestClient

    lateinit var service: DomainEventDetailService

    @BeforeEach
    fun beforeEach() {
        service = DomainEventDetailService(client, listOf("http://localhost:8080"))
    }

    @Test
    fun `missing URL is rejected`() {
        val exception = assertThrows<IllegalArgumentException> {
            service.getDetail<Any>(
                HmppsDomainEvent(
                    eventType = "test",
                    version = 1
                )
            )
        }
        assertThat(exception.message, equalTo("Detail URL must not be null"))
    }

    @Test
    fun `invalid URL is rejected`() {
        assertThrows<IllegalArgumentException> {
            service.getDetail<Any>(
                HmppsDomainEvent(
                    eventType = "test",
                    version = 1,
                    detailUrl = "invalid url"
                )
            )
        }
    }

    @Test
    fun `URL must be in allowed list`() {
        val exception = assertThrows<IllegalArgumentException> {
            service.getDetail<Any>(
                HmppsDomainEvent(
                    eventType = "test",
                    version = 1,
                    detailUrl = "https://example.com"
                )
            )
        }
        assertThat(exception.message, equalTo("Unexpected detail URL: https://example.com"))
    }

    @Test
    fun `API is called if URL is valid`() {
        whenever(client.get().uri(any<URI>()).retrieve().body(any<ParameterizedTypeReference<String>>()))
            .thenReturn("API Response")
        val response = service.getDetail<String>(
            HmppsDomainEvent(
                eventType = "test",
                version = 1,
                detailUrl = "http://localhost:8080"
            )
        )
        assertThat(response, equalTo("API Response"))
    }
}