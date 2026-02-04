package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.api.model.Document
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DocumentIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `document is downloaded`() {
        mockMvc.get("/document/X000004/00000000-0000-0000-0000-000000000001") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            withToken()
        }
            .andExpect { request { asyncStarted() } }
            .asyncDispatch()
            .andExpect {
                status { is2xxSuccessful() }
                header { string("Content-Type", "application/octet-stream") }
                header {
                    string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?doc1?=\"; filename*=UTF-8''doc1"
                    )
                }
                header { doesNotExist("Custom-Alfresco-Header") }
                content { bytes(ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes()) }
            }
    }

    @Test
    fun `approved premises documents data is returned`() {
        val res = mockMvc.get("/ap-residence-plan-document/X000010") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<Document>()

        assertThat(res.author, equalTo("Steve Smith"))
        assertThat(res.id, equalTo("00000000-0000-0000-0000-000000000003"))
        assertThat(res.name, equalTo("ap_doc_3"))
    }

    @Test
    fun `approved premises data is not found`() {
        mockMvc.get("/ap-residence-plan-document/X000004") { withToken() }
            .andExpect { status { isNotFound() } }
    }
}
