package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.toAddress
import uk.gov.justice.digital.hmpps.model.BasicDetails
import uk.gov.justice.digital.hmpps.model.Name
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest
class BasicDetailsIntegrationTest {

    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var wireMockServer: WireMockServer

    @Test
    fun `can retrieve all basic details`() {
        val person = PersonGenerator.DEFAULT_PERSON

        val response = mockMvc
            .perform(get("/basic-details/${person.crn}").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<BasicDetails>()

        assertThat(response).isEqualTo(
            BasicDetails(
                title = null,
                name = Name(
                    person.firstName,
                    listOfNotNull(person.secondName, person.thirdName).joinToString(" "),
                    person.surname
                ),
                addresses = listOf(PersonGenerator.DEFAULT_ADDRESS.toAddress()),
                prisonNumber = person.prisonerNumber,
                dateOfBirth = person.dateOfBirth
            )
        )
    }
}