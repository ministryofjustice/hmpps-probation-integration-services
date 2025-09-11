package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentLevel
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentSearch
import uk.gov.justice.digital.hmpps.api.model.personalDetails.DocumentTextSearch
import uk.gov.justice.digital.hmpps.api.model.personalDetails.PersonDocuments
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_2
import uk.gov.justice.digital.hmpps.service.DocumentLevelCode
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDateTime

class DocumentsIntegrationTest: IntegrationTestBase() {

    @Test
    fun `find all documents with default sort and page`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/documents/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()

        assertThat(res.documents.size, equalTo(5))
        assertThat(res.documents[0].name, equalTo("event report"))
        assertThat(res.documents[1].name, equalTo("court report"))
        assertThat(res.documents[2].name, equalTo("dic.doc"))
        assertThat(res.documents[3].name, equalTo("contact2.doc"))
        assertThat(res.documents[3].workInProgress, equalTo(false))
        assertThat(res.documents[3].status, equalTo(null))
        assertThat(res.documents[4].name, equalTo("contact.doc"))
        assertThat(res.documents[4].workInProgress, equalTo(true))
        assertThat(res.documents[4].status, equalTo("Sensitive"))
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
        assertThat(res.documents[1].name, equalTo("dic.doc"))
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

        assertThat(res.documents.size, equalTo(5))
        assertThat(res.documents[0].name, equalTo("event report"))
        assertThat(res.documents[1].name, equalTo("court report"))
        assertThat(res.documents[2].name, equalTo("dic.doc"))
        assertThat(res.documents[3].name, equalTo("contact2.doc"))
        assertThat(res.documents[3].workInProgress, equalTo(false))
        assertThat(res.documents[3].status, equalTo(null))
        assertThat(res.documents[4].name, equalTo("contact.doc"))
        assertThat(res.documents[4].workInProgress, equalTo(true))
        assertThat(res.documents[4].status, equalTo("Sensitive"))
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
        val expectedMetadata =
            (listOf(DocumentLevelCode.ALL) + (DocumentLevelCode.entries.filter { it != DocumentLevelCode.ALL }
                .sortedBy { it.name })).map { DocumentLevel(it.name, it.description) }
        assertThat(res.documents.size, equalTo(3))
        assertThat(res.documents[0].name, equalTo("dic.doc"))
        assertThat(res.documents[1].name, equalTo("contact2.doc"))
        assertThat(res.documents[2].name, equalTo("contact.doc"))
        assertThat(res.metadata?.documentLevels, equalTo(expectedMetadata))
    }

    @Test
    fun `find all documents using the alfresco text search returning all records maintaining search sort`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search/text").withToken()
                    .withJson(
                        DocumentTextSearch(
                            query = "text"
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()
        assertThat(res.documents.size, equalTo(4))
        assertThat(res.documents[0].alfrescoId, equalTo("B001"))
        assertThat(res.documents[1].alfrescoId, equalTo("B002"))
        assertThat(res.documents[2].alfrescoId, equalTo("A003"))
        assertThat(res.documents[3].alfrescoId, equalTo("A004"))
    }

    @Test
    fun `find all documents using the alfresco text search maintaining passed in sort and overall pagination`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search/text?sortBy=createdAt.desc&page=1&size=2").withToken()
                    .withJson(
                        DocumentTextSearch(
                            query = "text"
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()
        assertThat(res.totalElements, equalTo(4))
        assertThat(res.documents.size, equalTo(2))
        assertThat(res.documents[0].alfrescoId, equalTo("B002"))
        assertThat(res.documents[1].alfrescoId, equalTo("B001"))
    }

    @Test
    fun `find all documents with text search and no query goes straight to the DB - ordered by DB`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search/text?sortBy=createdAt.desc&page=1&size=2").withToken()
                    .withJson(
                        DocumentTextSearch(
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()
        assertThat(res.totalElements, equalTo(5))
        assertThat(res.documents.size, equalTo(2))
        assertThat(res.documents[0].alfrescoId, equalTo("C001"))
        assertThat(res.documents[1].alfrescoId, equalTo("B002"))
    }

    @Test
    fun `find only contact documents documents using the alfresco text search maintaining overall pagination - ordered by search results`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search/text").withToken()
                    .withJson(
                        DocumentTextSearch(
                            query = "text",
                            levelCode = DocumentLevelCode.CONTACT
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()
        val expectedMetadata =
            (listOf(DocumentLevelCode.ALL) + (DocumentLevelCode.entries.filter { it != DocumentLevelCode.ALL }
                .sortedBy { it.name })).map { DocumentLevel(it.name, it.description) }
        assertThat(res.totalElements, equalTo(2))
        assertThat(res.documents.size, equalTo(2))
        assertThat(res.documents[0].alfrescoId, equalTo("B001"))
        assertThat(res.documents[1].alfrescoId, equalTo("B002"))
        assertThat(res.metadata?.documentLevels, equalTo(expectedMetadata))
    }

    @Test
    fun `find all documents using the alfresco text search and also the DB filename with multiple keywords`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search/text?useDBFilenameSearch=true").withToken()
                    .withJson(
                        DocumentTextSearch(
                            query = "text dic",
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()
        val expectedMetadata =
            (listOf(DocumentLevelCode.ALL) + (DocumentLevelCode.entries.filter { it != DocumentLevelCode.ALL }
                .sortedBy { it.name })).map { DocumentLevel(it.name, it.description) }
        assertThat(res.totalElements, equalTo(5))
        assertThat(res.documents.size, equalTo(5))
        assertThat(res.documents[0].alfrescoId, equalTo("B001"))
        assertThat(res.documents[1].alfrescoId, equalTo("B002"))
        assertThat(res.documents[2].alfrescoId, equalTo("A003"))
        assertThat(res.documents[3].alfrescoId, equalTo("A004"))
        assertThat(res.documents[4].alfrescoId, equalTo("C001"))
        assertThat(res.metadata?.documentLevels, equalTo(expectedMetadata))
    }

    @Test
    fun `find all documents using the alfresco text search without the DB filename search with multiple keywords`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search/text?useDBFilenameSearch=false").withToken()
                    .withJson(
                        DocumentTextSearch(
                            query = "text dic",
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()
        val expectedMetadata =
            (listOf(DocumentLevelCode.ALL) + (DocumentLevelCode.entries.filter { it != DocumentLevelCode.ALL }
                .sortedBy { it.name })).map { DocumentLevel(it.name, it.description) }
        assertThat(res.totalElements, equalTo(4))
        assertThat(res.documents.size, equalTo(4))
        assertThat(res.documents[0].alfrescoId, equalTo("B001"))
        assertThat(res.documents[1].alfrescoId, equalTo("B002"))
        assertThat(res.documents[2].alfrescoId, equalTo("A003"))
        assertThat(res.documents[3].alfrescoId, equalTo("A004"))
        assertThat(res.metadata?.documentLevels, equalTo(expectedMetadata))
    }

    @Test
    fun `when alfresco client throws error, return no documents but returns metadata`() {
        val person = PERSON_2
        val res = mockMvc
            .perform(
                post("/documents/${person.crn}/search/text?useDBFilenameSearch=true").withToken()
                    .withJson(
                        DocumentTextSearch(
                            query = "-",
                        )
                    )
            )
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonDocuments>()
        val expectedMetadata =
            (listOf(DocumentLevelCode.ALL) + (DocumentLevelCode.entries.filter { it != DocumentLevelCode.ALL }
                .sortedBy { it.name })).map { DocumentLevel(it.name, it.description) }
        assertThat(res.totalElements, equalTo(0))
        assertThat(res.documents.size, equalTo(0))
        assertThat(res.metadata?.documentLevels, equalTo(expectedMetadata))
    }
}
