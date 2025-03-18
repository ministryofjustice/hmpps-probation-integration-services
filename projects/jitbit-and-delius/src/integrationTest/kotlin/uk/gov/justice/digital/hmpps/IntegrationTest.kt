package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
}
