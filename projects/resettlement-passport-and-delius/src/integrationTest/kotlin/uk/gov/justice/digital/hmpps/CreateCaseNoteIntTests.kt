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
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.api.model.CreateContact
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEFAULT
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator.DEFAULT_AREA
import uk.gov.justice.digital.hmpps.entity.AlertRepository
import uk.gov.justice.digital.hmpps.entity.CaseNoteRepository
import uk.gov.justice.digital.hmpps.entity.StaffRepository
import uk.gov.justice.digital.hmpps.entity.TeamRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class CreateCaseNoteIntTests {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Autowired
    internal lateinit var caseNoteRepository: CaseNoteRepository

    @Autowired
    internal lateinit var alertRepository: AlertRepository

    @Autowired
    internal lateinit var staffRepository: StaffRepository

    @Autowired
    internal lateinit var teamRepository: TeamRepository

    @Test
    fun `create contact for RP9 when author does not exist`() {
        val crn = DEFAULT.crn
        mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "IMMEDIATE_NEEDS_REPORT",
                        "notes": "Testing",
                        "dateTime": "2023-02-12T10:15:00.382936Z[Europe/London]",
                        "author": {
                          "forename": "John",
                          "surname": "Brown",
                          "prisonCode": "LDN"
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().isCreated)

        val contact = caseNoteRepository.findByPersonIdAndTypeCode(
            DEFAULT.id,
            CreateContact.CaseNoteType.IMMEDIATE_NEEDS_REPORT.code
        ).first()
        val alert =
            alertRepository.findByContactId(contact.id)
        val staff = staffRepository.findById(contact.staffId).get()
        val team = teamRepository.findById(contact.teamId).get()
        assertThat(alert?.contactId, equalTo(contact.id))
        assertThat(alert?.personManagerId, equalTo(PersonGenerator.DEFAULT_MANAGER.id))
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.description, equalTo(null))
        assertThat(contact.notes, equalTo("Testing"))
        assertThat(contact.type.code, equalTo(CreateContact.CaseNoteType.IMMEDIATE_NEEDS_REPORT.code))
        assertThat(staff.forename, equalTo("John"))
        assertThat(staff.surname, equalTo("Brown"))
        assertThat(staff.code, equalTo("LDNA002"))
        assertThat(team.code, equalTo("LDNCSN"))
        assertThat(contact.probationAreaId, equalTo(DEFAULT_AREA.id))
    }

    @Test
    fun `create contact for RP10 where author already exists`() {
        val crn = DEFAULT.crn
        mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "PRE_RELEASE_REPORT",
                        "description": "Part 1 of 2",
                        "notes": "Testing",
                        "dateTime": "2023-02-12T10:15:00.382936Z[Europe/London]",
                        "author": {
                          "forename": "Terry",
                          "surname": "Nutkins",
                          "prisonCode": "LDN"
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().isCreated)

        val contact =
            caseNoteRepository.findByPersonIdAndTypeCode(DEFAULT.id, CreateContact.CaseNoteType.PRE_RELEASE_REPORT.code)
                .first()
        val alert =
            alertRepository.findByContactId(contact.id)
        val staff = staffRepository.findById(contact.staffId).get()
        val team = teamRepository.findById(contact.teamId).get()
        assertThat(alert?.contactId, equalTo(contact.id))
        assertThat(alert?.personManagerId, equalTo(PersonGenerator.DEFAULT_MANAGER.id))
        assertThat(alert?.contactId, equalTo(contact.id))
        assertThat(contact.alert, equalTo(true))
        assertThat(contact.description, equalTo("Part 1 of 2"))
        assertThat(contact.notes, equalTo("Testing"))
        assertThat(contact.type.code, equalTo(CreateContact.CaseNoteType.PRE_RELEASE_REPORT.code))
        assertThat(staff.forename, equalTo("Terry"))
        assertThat(staff.surname, equalTo("Nutkins"))
        assertThat(staff.code, equalTo("LDNA001"))
        assertThat(team.code, equalTo("LDNCSN"))
        assertThat(contact.probationAreaId, equalTo(DEFAULT_AREA.id))
    }

    @Test
    fun `return bad request when author prison code is not found`() {
        val crn = DEFAULT.crn
        val res = mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "PRE_RELEASE_REPORT",
                        "notes": "Testing",
                        "dateTime": "2023-02-12T10:15:00.382936Z[Europe/London]",
                        "author": {
                          "forename": "John",
                          "surname": "Brown",
                          "prisonCode": "NHJ"
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().isBadRequest).andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Probation Area not found for NOMIS institution: NHJ"))
    }

    @Test
    fun `return bad request when team code not found for prison code`() {
        val crn = DEFAULT.crn
        val res = mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "PRE_RELEASE_REPORT",
                        "notes": "Testing",
                        "dateTime": "2023-02-12T10:15:00.382936Z[Europe/London]",
                        "author": {
                          "forename": "John",
                          "surname": "Brown",
                          "prisonCode": "MDL"
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().isBadRequest).andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(res.message, equalTo("Team with code of MDLCSN not found"))
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
                        "type": "OTHER",
                        "notes": "Testing",
                        "dateTime": "2023-02-12T10:15:00.382936Z[Europe/London]",
                        "author": {
                          "forename": "John",
                          "surname": "Brown",
                          "prisonCode": "LDN"
                        }
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
                        "type": "PRE_RELEASE_REPORT",
                        "notes": "Testing",
                        "dateTime": "",
                        "author": {
                          "forename": "John",
                          "surname": "Brown",
                          "prisonCode": "LDN"
                        }
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
                        "type": "PRE_RELEASE_REPORT",
                        "notes": "",
                        "dateTime": "2024-02-12T10:15:00.382936Z[Europe/London]",
                        "author": {
                          "forename": "John",
                          "surname": "Brown",
                          "prisonCode": "LDN"
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `return bad request when no author provided`() {
        val crn = DEFAULT.crn
        mockMvc.perform(
            post("/nomis-case-note/${crn}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "type": "PRE_RELEASE_REPORT",
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
                        "type": "PRE_RELEASE_REPORT",
                        "notes": "Testing",
                        "dateTime": "2024-02-12T10:15:00.382936Z[Europe/London]",
                        "author": {
                          "forename": "John",
                          "surname": "Brown",
                          "prisonCode": "LDN"
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().isNotFound)
    }
}