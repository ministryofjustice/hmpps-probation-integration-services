package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.api.model.OffenderDocuments
import uk.gov.justice.digital.hmpps.api.resource.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class DocumentsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `call documents grouped by CRN`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        val response = mockMvc.get("/probation-case/$crn/documents/grouped") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<OffenderDocuments>()

        assertThat(response.documents.size, equalTo(0))
        assertThat(response.convictions.size, equalTo(1))
    }

    @Test
    fun `call documents grouped by CRN invalid filter`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        val response = mockMvc.get("/probation-case/$crn/documents/grouped?type=INVALID") { withToken() }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.developerMessage, equalTo("type of INVALID was not valid"))
    }

    @Test
    fun `call download document by id`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        mockMvc.get("/probation-case/$crn/documents/00000000-0000-0000-0000-000000000001") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            withToken()
        }
            .andExpect { request { asyncStarted() } }
            .asyncDispatch()
            .andExpect {
                status { is2xxSuccessful() }
                header {
                    string("Content-Type", "application/msword;charset=UTF-8")

                    string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?filename.txt?=\"; filename*=UTF-8''filename.txt"
                    )

                    doesNotExist("Custom-Alfresco-Header")
                }

                content {
                    bytes(ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes())
                }
            }
    }

    @Test
    fun `call download document by id returns not found`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        val response = mockMvc.get("/probation-case/$crn/documents/wrong") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>()

        assertThat(response.developerMessage, equalTo("Document with id of wrong not found for CRN C123456"))
    }
}