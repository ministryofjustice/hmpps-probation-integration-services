package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.Address
import uk.gov.justice.digital.hmpps.api.model.OfficeLocation
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OfficeLocationResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `correctly returns all CRS office locations`() {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/office-locations")
                .withOAuth2Token(wireMockServer)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().is2xxSuccessful).andReturn().response.contentAsString

        val officeLocations = objectMapper.readValue<List<OfficeLocation>>(res)
        assertThat(officeLocations.size, equalTo(3))
        assertThat(
            officeLocations,
            hasItem(
                OfficeLocation(
                    "TESTONE",
                    "Description of TESTONE",
                    Address.from(buildingName = "Test One", streetName = "Mantle Place", postcode = "MP1 1PM"),
                    null
                )
            )
        )
        assertThat(
            officeLocations,
            hasItem(
                OfficeLocation(
                    "TESTTWO",
                    "Description of TESTTWO",
                    Address.from(buildingName = "Test Two", postcode = "MP2 2PM"),
                    telephoneNumber = "020 123 6789"
                )
            )
        )
        assertThat(
            officeLocations,
            hasItem(
                OfficeLocation(
                    "DEFAULT",
                    description = "Description of DEFAULT",
                    address = null,
                    telephoneNumber = null
                )
            )
        )
    }
}
