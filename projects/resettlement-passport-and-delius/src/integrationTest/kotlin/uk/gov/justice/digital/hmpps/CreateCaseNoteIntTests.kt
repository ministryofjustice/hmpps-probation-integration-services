package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.CreateContact
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT
import uk.gov.justice.digital.hmpps.entity.CaseNoteRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CreateCaseNoteIntTests {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var caseNoteRepository: CaseNoteRepository

    @Test
    fun `create contact for RP9`() {
        val crn = DEFAULT.crn
        mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "RP9",
                        "notes": "Testing",
                        "dateTime": "2023-02-12T10:15:00.382936Z[Europe/London]"
                    }
                """
                )
                .withToken()
        ).andExpect(status().isCreated)

        val contact = caseNoteRepository.findByPersonIdAndTypeCode(DEFAULT.id, CreateContact.Type.RP9.name).first()
        assertThat(contact.notes, equalTo("Testing"))
        assertThat(contact.type.code, equalTo(CreateContact.Type.RP9.name))
    }

    @Test
    fun `create contact for RP10`() {
        val crn = DEFAULT.crn
        mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "RP10",
                        "notes": "Testing",
                        "dateTime": "2023-02-12T10:15:00.382936Z[Europe/London]"
                    }
                """
                )
                .withToken()
        ).andExpect(status().isCreated)

        val contact = caseNoteRepository.findByPersonIdAndTypeCode(DEFAULT.id, CreateContact.Type.RP10.name).first()
        assertThat(contact.notes, equalTo("Testing"))
        assertThat(contact.type.code, equalTo(CreateContact.Type.RP10.name))
    }

    @Test
    fun `return bad request when not RP9 or RP10`() {
        val crn = DEFAULT.crn
        mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "RP8",
                        "notes": "Testing",
                        "dateTime": "2023-02-12T10:15:00.382936Z[Europe/London]"
                    }
                """
                )
                .withToken()
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `return bad request when an invalid datetime is provided`() {
        val crn = DEFAULT.crn
        mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "RP9",
                        "notes": "Testing",
                        "dateTime": ""
                    }
                """
                )
                .withToken()
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `return bad request when no notes provided`() {
        val crn = DEFAULT.crn
        mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "RP9",
                        "notes": "",
                        "dateTime": "2024-02-12T10:15:00.382936Z[Europe/London]"
                    }
                """
                )
                .withToken()
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `return not found when crn is not found`() {
        mockMvc.perform(
            post("/nomis-case-note/X123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "RP10",
                        "notes": "Testing",
                        "dateTime": "2024-02-12T10:15:00.382936Z[Europe/London]"
                    }
                """
                )
                .withToken()
        ).andExpect(status().isNotFound)
    }
}