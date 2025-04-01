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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonDocuments
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

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
        assertThat(res.documents[2].status, equalTo(null))
        assertThat(res.documents[3].name, equalTo("contact.doc"))
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
}
