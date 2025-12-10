package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.sentence.Address
import uk.gov.justice.digital.hmpps.api.model.sentence.LocationDetails
import uk.gov.justice.digital.hmpps.api.model.sentence.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.UserOfficeLocation
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class UserLocationIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unauthorized status returned`() {
        mockMvc.get("/user/peter-parker/locations")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `user not found`() {
        mockMvc.get("/user/user/locations") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value(equalTo("User with username of user not found")) }
            }
    }

    @Test
    fun `get user locations`() {
        val response = mockMvc.get("/user/peter-parker/locations") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<UserOfficeLocation>()

        val expected = UserOfficeLocation(
            Name(STAFF_USER_1.forename, surname = STAFF_USER_1.surname),
            listOf(
                LocationDetails(
                    DEFAULT_LOCATION.id,
                    DEFAULT_LOCATION.code,
                    DEFAULT_LOCATION.description,
                    Address(
                        DEFAULT_LOCATION.buildingNumber,
                        DEFAULT_LOCATION.streetName,
                        DEFAULT_LOCATION.townCity,
                        DEFAULT_LOCATION.county,
                        DEFAULT_LOCATION.postcode
                    )
                )
            )
        )
        assertEquals(expected, response)
    }
}