package uk.gov.justice.digital.hmpps

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
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DISTRICT_BRK
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.LOCATION_BRK_1
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.LOCATION_BRK_2
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.generateOfficeAddress
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class OfficeAddressIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun badRequest() {
        mockMvc
            .perform(get("/office/addresses").withToken())
            .andExpect(status().isBadRequest)
    }

    @ParameterizedTest
    @MethodSource("officeAddressArgs")
    fun getOfficeAddress(url: String, pageSize: Int, resultSize: Int, pageNumber: Int, results: List<OfficeAddress>?) {
        val res = mockMvc
            .perform(get(url).withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<ResultSet<OfficeAddress>>()

        assertThat(res.totalPages, equalTo(pageSize))
        assertThat(res.results.size, equalTo(resultSize))
        assertThat(res.page, equalTo(pageNumber))
        assertThat(res.results, equalTo(results))
    }

    companion object {
        @JvmStatic
        fun officeAddressArgs(): List<Arguments> = listOf(
            Arguments.of("/office/addresses?ldu=Berk&officeName=nothing", 0, 0, 0, listOf<OfficeAddress>()),
            Arguments.of(
                "/office/addresses?ldu=Berk&officeName=Office", 1, 2, 0,
                listOf(
                    generateOfficeAddress(LOCATION_BRK_1, DISTRICT_BRK),
                    generateOfficeAddress(LOCATION_BRK_2, DISTRICT_BRK)
                )
            ),
            Arguments.of(
                "/office/addresses?ldu=Berk&officeName=Reading", 1, 1, 0,
                listOf(generateOfficeAddress(LOCATION_BRK_2, DISTRICT_BRK))
            ),
            Arguments.of(
                "/office/addresses?ldu=Berk&officeName=Brack", 1, 1, 0,
                listOf(generateOfficeAddress(LOCATION_BRK_1, DISTRICT_BRK))
            ),
            Arguments.of(
                "/office/addresses?page=0&size=1&ldu=Berk&officeName=Office", 2, 1, 0,
                listOf(generateOfficeAddress(LOCATION_BRK_1, DISTRICT_BRK))
            ),
            Arguments.of(
                "/office/addresses?page=1&size=1&ldu=Berk&officeName=Office", 2, 1, 1,
                listOf(generateOfficeAddress(LOCATION_BRK_2, DISTRICT_BRK))
            ),
            Arguments.of(
                "/office/addresses?page=1&size=1&ldu=berk&officeName=office", 2, 1, 1,
                listOf(generateOfficeAddress(LOCATION_BRK_2, DISTRICT_BRK))
            ),
        )
    }
}
