package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator.LOCATION_1
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator.LOCATION_2
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator.BOROUGH_1
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator.DEFAULT_BOROUGH
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator.DEFAULT_TEAM
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator.PROVIDER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator.PROVIDER_2
import uk.gov.justice.digital.hmpps.entity.asAddress
import uk.gov.justice.digital.hmpps.model.OfficeAddress
import uk.gov.justice.digital.hmpps.model.ProbationDeliveryUnit
import uk.gov.justice.digital.hmpps.model.Provider
import uk.gov.justice.digital.hmpps.service.toPdu
import uk.gov.justice.digital.hmpps.service.toProvider
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProviderIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `get all providers`() {
        val response = mockMvc
            .get("/providers") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<Provider>>()

        val expected = listOf(DEFAULT_PROVIDER.toProvider(), PROVIDER_1.toProvider(), PROVIDER_2.toProvider())

        assertThat(
            response,
            equalTo(expected)
        )
    }

    @Test
    fun `get all pdus for provider`() {
        val response = mockMvc
            .get("/providers/${PROVIDER_1.code.trim()}/pdus") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<ProbationDeliveryUnit>>()

        val expected = listOf(BOROUGH_1.toPdu())

        assertThat(
            response,
            equalTo(expected)
        )
    }

    @Test
    fun `get all office locations for provider and pdu`() {
        val response = mockMvc
            .get("/providers/${DEFAULT_PROVIDER.code.trim()}/pdus/${DEFAULT_BOROUGH.code.trim()}/locations") {
                withToken()
            }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<OfficeAddress>>()

        val expected =
            listOf(LOCATION_1.asAddress(DEFAULT_TEAM.emailAddress), LOCATION_2.asAddress(DEFAULT_TEAM.emailAddress))

        assertThat(
            response,
            equalTo(expected)
        )
    }
}
