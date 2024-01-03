package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.OfficeAddress
import uk.gov.justice.digital.hmpps.api.resource.ResultSet
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator.DISTRICT_BRK
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator.LOCATION_BRK_1
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator.LOCATION_BRK_2
import uk.gov.justice.digital.hmpps.data.generator.OfficeLocationGenerator.generateOfficeAddress
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class OfficeAddressIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper



    @Test
    fun badRequest() {
        mockMvc
            .perform(get("/office/addresses").withOAuth2Token(wireMockServer))
            .andExpect(status().isBadRequest)
    }

    @ParameterizedTest
    @MethodSource("officeAddressArgs")
    fun getOfficeAddress(url: String, pageSize: Int, resultSize: Int, pageNumber: Int, results: List<OfficeAddress>?) {
        val res = mockMvc
            .perform(get(url).withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
        val rs = objectMapper.readValue<ResultSet<OfficeAddress>>(res)
        assertThat(rs.totalPages, equalTo(pageSize))
        assertThat(rs.results.size, equalTo(resultSize))
        assertThat(rs.page, equalTo(pageNumber))
        assertThat(rs.results, equalTo(results))
    }

    companion object {
        @JvmStatic
        fun officeAddressArgs(): List<Arguments> = listOf(
            Arguments.of("/office/addresses?ldu=Berk&officeName=nothing", 0, 0, 0, listOf<OfficeAddress>()),
            Arguments.of("/office/addresses?ldu=Berk&officeName=Office", 1, 2, 0,
                listOf(generateOfficeAddress(LOCATION_BRK_1, DISTRICT_BRK), generateOfficeAddress(LOCATION_BRK_2, DISTRICT_BRK))),
            Arguments.of("/office/addresses?ldu=Berk&officeName=Reading", 1, 1, 0,
                listOf(generateOfficeAddress(LOCATION_BRK_2, DISTRICT_BRK))),
            Arguments.of("/office/addresses?ldu=Berk&officeName=Brack", 1, 1, 0,
                listOf(generateOfficeAddress(LOCATION_BRK_1, DISTRICT_BRK))),
            Arguments.of("/office/addresses?page=0&size=1&ldu=Berk&officeName=Office", 2, 1, 0,
                listOf(generateOfficeAddress(LOCATION_BRK_1, DISTRICT_BRK))),
            Arguments.of("/office/addresses?page=1&size=1&ldu=Berk&officeName=Office", 2, 1, 1,
                listOf(generateOfficeAddress(LOCATION_BRK_2, DISTRICT_BRK))),
        )
    }
}
