package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.Contact
import uk.gov.justice.digital.hmpps.api.model.sentence.ProfessionalContact
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

class ContactIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unauthorized status returned`() {
        mockMvc.get("/sentence/X123456/contacts")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `person does not exist`() {
        val result = mockMvc.get("/sentence/X123456/contacts") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn()

        assertEquals(
            "Person with crn of X123456 not found",
            result.resolvedException?.message
        )
    }

    @Test
    fun `no offender manager records`() {
        val result = mockMvc.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/contacts") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn()

        assertEquals(
            "Offender Manager records with crn of ${PersonDetailsGenerator.PERSONAL_DETAILS.crn} not found",
            result.resolvedException?.message
        )
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
            prisonOffenderManager = false,
            isUnallocated = false
        )
        val contact2 =
            Contact(
                "Bruce Wayne",
                "bruce.wayne@moj.gov.uk",
                "07321165777",
                "Description of N01",
                "Leicestershire All",
                "OMU B",
                LocalDate.of(2025, 2, 9),
                LocalDate.of(2025, 2, 10),
                LocalDate.of(2025, 2, 9),
                responsibleOfficer = false,
                prisonOffenderManager = false,
                isUnallocated = false
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
            prisonOffenderManager = true,
            isUnallocated = false
        )

        val contact4 = Contact(
            "Unallocated Staff",
            null,
            null,
            "Description of N01",
            "Leicestershire All",
            "OMU B",
            LocalDate.of(2025, 2, 9),
            LocalDate.of(2025, 2, 10),
            lastUpdated = LocalDate.of(2025, 2, 9),
            responsibleOfficer = false,
            prisonOffenderManager = false,
            isUnallocated = true
        )

        val expected =
            ProfessionalContact(name, currentContacts = listOf(contact1, contact3), previousContacts = listOf(contact2, contact4))

        val response = mockMvc.get("/sentence/${PersonGenerator.OVERVIEW.crn}/contacts") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<ProfessionalContact>()

        assertEquals(expected, response)
    }
}