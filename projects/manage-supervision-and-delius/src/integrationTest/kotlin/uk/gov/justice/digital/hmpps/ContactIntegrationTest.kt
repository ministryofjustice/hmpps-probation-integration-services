package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.Contact
import uk.gov.justice.digital.hmpps.api.model.sentence.ProfessionalContact
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContactIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/X123456/contacts"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `person does not exist`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/X123456/contacts")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect { result: MvcResult ->
                assertEquals(
                    "Person with crn of X123456 not found",
                    result.resolvedException!!.message
                )
            }
    }

    @Test
    fun `no contacts`() {

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OFFENDER_WITHOUT_EVENTS.crn}/contacts")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect { result: MvcResult ->
                assertEquals(
                    "Offender Manager with crn of ${PersonGenerator.OFFENDER_WITHOUT_EVENTS.crn} not found",
                    result.resolvedException!!.message
                )
            }
    }

    @Test
    fun `return additional offences`() {
        val name = Name(
            PersonGenerator.OVERVIEW.forename,
            PersonGenerator.OVERVIEW.secondName,
            PersonGenerator.OVERVIEW.surname
        )

        val contact1 = Contact(
            "Peter Parker",
            "peter.parker@moj.gov.uk",
            "07321165373",
            "Description of N01",
            "Leicestershire All",
            "OMU B",
            null
        )
        val contact2 =
            Contact("Bruce Wayne", null, null, "Description of N01", "Leicestershire All", "OMU B", LocalDate.now())

        val expected = ProfessionalContact(name, listOf(contact1, contact2))

        val response = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}/contacts")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<ProfessionalContact>()

        assertEquals(expected, response)
    }
}