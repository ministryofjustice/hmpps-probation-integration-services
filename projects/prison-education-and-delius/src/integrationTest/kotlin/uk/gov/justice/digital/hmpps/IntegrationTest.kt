package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.PERSON
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `get community manager`() {
        mockMvc.get("/probation-case/${PERSON.prisonerId}/community-manager") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }

                jsonPath("firstName") { value(equalTo("Test")) }
                jsonPath("lastName") { value(equalTo("User")) }
                jsonPath("email") { value(equalTo("test@example.com")) }
            }
    }

    @Test
    fun `get main address`() {
        mockMvc.get("/probation-case/${PERSON.prisonerId}/main-address") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }

                jsonPath("buildingName") { value(equalTo("Building Name")) }
                jsonPath("addressNumber") { value(equalTo("123")) }
                jsonPath("streetName") { value(equalTo("Street Name")) }
                jsonPath("district") { value(equalTo("District")) }
                jsonPath("town") { value(equalTo("Town City")) }
                jsonPath("county") { value(equalTo("County")) }
                jsonPath("postcode") { value(equalTo("AA1 1AA")) }
                jsonPath("noFixedAbode") { value(equalTo(false)) }
            }
    }
}
