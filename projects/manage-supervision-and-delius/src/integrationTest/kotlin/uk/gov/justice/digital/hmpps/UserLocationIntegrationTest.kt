package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.sentence.Address
import uk.gov.justice.digital.hmpps.api.model.sentence.LocationDetails
import uk.gov.justice.digital.hmpps.api.model.sentence.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.UserOfficeLocation
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.DEFAULT_LOCATION
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserLocationIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/user/peter-parker/locations"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `get user location`() {
         val response = mockMvc.perform(MockMvcRequestBuilders.get("/user/peter-parker/locations").withToken())
             .andDo(print())
             .andExpect(MockMvcResultMatchers.status().isOk)
             .andReturn().response.contentAsJson<UserOfficeLocation>()

        val expected =  UserOfficeLocation(
            Name(STAFF_USER_1.forename, surname = STAFF_USER_1.surname),
            listOf(LocationDetails(
                DEFAULT_LOCATION.id,
                DEFAULT_LOCATION.description,
                Address(DEFAULT_LOCATION.buildingNumber, DEFAULT_LOCATION.streetName, DEFAULT_LOCATION.townCity, DEFAULT_LOCATION.county, DEFAULT_LOCATION.postcode))
            )
        )
        assertEquals(expected, response)
    }
}