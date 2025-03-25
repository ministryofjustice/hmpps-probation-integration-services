package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.EXCLUSION
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.RESTRICTION
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.RESTRICTION_EXCLUSION
import uk.gov.justice.digital.hmpps.model.LimitedAccessDetail
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns 404 when not found`() {
        mockMvc
            .perform(get("/case/NONEXISTENT").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `returns full details`() {
        mockMvc
            .perform(get("/case/A000001").withToken())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "name": {
                        "forename": "First",
                        "middleName": "Middle Middle 2",
                        "surname": "Last"
                      },
                      "dateOfBirth": "1980-01-01",
                      "mainAddress": {
                        "buildingName": "Building name",
                        "addressNumber": "123",
                        "streetName": "Street",
                        "townCity": "Town",
                        "district": "District",
                        "county": "County",
                        "postcode": "POSTCODE",
                        "noFixedAbode": false
                      }
                    }
                    """
                )
            )
    }

    @Test
    fun `returns basic details`() {
        mockMvc
            .perform(get("/case/A000002").withToken())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "name": {
                        "forename": "First",
                        "surname": "Last"
                      },
                      "dateOfBirth": "1980-01-01"
                    }
                    """
                )
            )
    }

    @ParameterizedTest
    @ValueSource(strings = ["E123456", "R123456", "B123456"])
    fun `does not return data for lao cases`(crn: String) {
        mockMvc
            .perform(get("/case/$crn").withToken())
            .andExpect(status().isForbidden)
            .andExpect(
                content().json(
                    """
                    {
                      "message": "Access has been denied as this case is a Limited Access case."
                    }
                    """
                )
            )
    }

    @ParameterizedTest
    @MethodSource("caseAccess")
    fun `returns LAO information correctly`(crn: String, limitedAccess: LimitedAccessDetail) {
        val response = mockMvc
            .perform(
                get("/case/$crn/access")
                    .withToken()
            )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<LimitedAccessDetail>()

        assertThat(response, equalTo(limitedAccess))
    }

    companion object {
        @JvmStatic
        fun caseAccess() = listOf(
            Arguments.of(DEFAULT.crn, LimitedAccessDetail(DEFAULT.crn, false, false)),
            Arguments.of(EXCLUSION.crn, LimitedAccessDetail(EXCLUSION.crn, true, false)),
            Arguments.of(RESTRICTION.crn, LimitedAccessDetail(RESTRICTION.crn, false, true)),
            Arguments.of(RESTRICTION_EXCLUSION.crn, LimitedAccessDetail(RESTRICTION_EXCLUSION.crn, true, true)),
        )
    }
}
