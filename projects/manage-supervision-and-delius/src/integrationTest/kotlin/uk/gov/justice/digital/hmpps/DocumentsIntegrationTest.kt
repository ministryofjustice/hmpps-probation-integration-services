package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentSearch
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonDocuments
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DocumentsIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `find all documents with default sort and page`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/documents/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()

        assertThat(res.documents.size, equalTo(4))
        assertThat(res.documents[0].name, equalTo("event report"))
        assertThat(res.documents[1].name, equalTo("court report"))
        assertThat(res.documents[2].name, equalTo("contact2.doc"))
        assertThat(res.documents[2].workInProgress, equalTo(false))
        assertThat(res.documents[2].status, equalTo(null))
        assertThat(res.documents[3].name, equalTo("contact.doc"))
        assertThat(res.documents[3].workInProgress, equalTo(true))
        assertThat(res.documents[3].status, equalTo("Sensitive"))
    }

    @Test
    fun `find all documents with sort and page`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/documents/${person.crn}?sortBy=name.desc&page=0&size=2").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()

        assertThat(res.documents.size, equalTo(2))
        assertThat(res.documents[0].name, equalTo("event report"))
        assertThat(res.documents[1].name, equalTo("court report"))
    }

    @Test
    fun `search all documents with default sort and page`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search").withToken()
                    .withJson(
                        DocumentSearch(
                            name = "co",
                            dateFrom = LocalDateTime.now().minusDays(20),
                            dateTo = LocalDateTime.now()
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()

        assertThat(res.documents.size, equalTo(3))
        assertThat(res.documents[0].name, equalTo("court report"))
        assertThat(res.documents[1].name, equalTo("contact2.doc"))
        assertThat(res.documents[2].name, equalTo("contact.doc"))
    }

    @Test
    fun `find all documents using no search params with default sort and page`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search").withToken()
                    .withJson(DocumentSearch())
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()

        assertThat(res.documents.size, equalTo(4))
        assertThat(res.documents[0].name, equalTo("event report"))
        assertThat(res.documents[1].name, equalTo("court report"))
        assertThat(res.documents[2].name, equalTo("contact2.doc"))
        assertThat(res.documents[2].workInProgress, equalTo(false))
        assertThat(res.documents[2].status, equalTo(null))
        assertThat(res.documents[3].name, equalTo("contact.doc"))
        assertThat(res.documents[3].workInProgress, equalTo(true))
        assertThat(res.documents[3].status, equalTo("Sensitive"))
    }

    @Test
    fun `find all documents using single day in to and from `() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search").withToken()
                    .withJson(
                        DocumentSearch(
                            dateTo = LocalDateTime.now().minusDays(16),
                            dateFrom = LocalDateTime.now().minusDays(16)
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()

        assertThat(res.documents.size, equalTo(2))
        assertThat(res.documents[0].name, equalTo("contact2.doc"))
        assertThat(res.documents[1].name, equalTo("contact.doc"))
    }
}
