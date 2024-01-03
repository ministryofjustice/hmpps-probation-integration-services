package uk.gov.justice.digital.hmpps

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.Address
import uk.gov.justice.digital.hmpps.api.model.OfficeLocation
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OfficeLocationResourceTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `correctly returns all CRS office locations`() {
        val officeLocations = mockMvc.perform(get("/office-locations").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<OfficeLocation>>()

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
