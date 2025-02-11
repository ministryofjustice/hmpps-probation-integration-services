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
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
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
    fun `no offender manager records`() {

        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/contacts")
                    .withToken()
            )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect { result: MvcResult ->
                assertEquals(
                    "Offender Manager records with crn of ${PersonDetailsGenerator.PERSONAL_DETAILS.crn} not found",
                    result.resolvedException!!.message
                )
            }
    }

    @Test
    fun `return offender manager records`() {
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
            LocalDate.of(2025, 2, 10),
            allocatedUntil = null,
            lastUpdated = LocalDate.of(2025, 2, 10),
            responsibleOfficer = false,
            prisonOffenderManager = false
        )
        val contact2 =
            Contact(
                "Bruce Wayne",
                null,
                null,
                "Description of N01",
                "Leicestershire All",
                "OMU B",
                LocalDate.of(2025, 2, 9),
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 2, 9),
                responsibleOfficer = false,
                prisonOffenderManager = false
            )

        val contact3 = Contact(
            "Clark Kent",
            null,
            null,
            "Description of N01",
            "Leicestershire All",
            "OMU B",
            LocalDate.of(2025, 2, 7),
            allocatedUntil = null,
            lastUpdated = LocalDate.of(2025, 2, 7),
            responsibleOfficer = true,
            prisonOffenderManager = true
        )

        val expected =
            ProfessionalContact(name, currentContacts = listOf(contact1, contact3), previousContacts = listOf(contact2))

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