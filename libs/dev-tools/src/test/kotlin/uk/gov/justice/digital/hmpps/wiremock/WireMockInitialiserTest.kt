package uk.gov.justice.digital.hmpps.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.ConfigurableEnvironment

@ExtendWith(MockitoExtension::class)
internal class WireMockInitialiserTest {
    @Mock
    private lateinit var ctx: ConfigurableApplicationContext

    @Mock
    private lateinit var env: ConfigurableEnvironment

    @Mock
    private lateinit var beanFactory: ConfigurableListableBeanFactory

    private lateinit var wireMockInitialiser: WireMockInitialiser

    @BeforeEach
    fun setup() {
        wireMockInitialiser = WireMockInitialiser()
    }

    @Test
    fun `when integration test profile is set then wiremock server initialised`() {
        val systemProperties = mutableMapOf<String, Any>()
        whenever(ctx.environment).thenReturn(env)
        whenever(env.activeProfiles).thenReturn(arrayOf("integration-test"))
        whenever(env.systemProperties).thenReturn(systemProperties)
        whenever(ctx.beanFactory).thenReturn(beanFactory)

        wireMockInitialiser.initialize(ctx)

        val wiremockCaptor = ArgumentCaptor.forClass(WireMockServer::class.java)
        verify(beanFactory).registerSingleton(eq("wireMockServer"), wiremockCaptor.capture())

        val wireMockServer = wiremockCaptor.value
        assertNotNull(wireMockServer)
        assertThat(ctx.environment.systemProperties["wiremock.port"], equalTo(wireMockServer.port().toString()))
    }
}
