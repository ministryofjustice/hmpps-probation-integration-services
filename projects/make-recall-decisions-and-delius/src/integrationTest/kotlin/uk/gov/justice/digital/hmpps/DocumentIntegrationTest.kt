package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.api.model.Document
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DocumentIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `document is downloaded`() {
        mockMvc.perform(get("/document/X000004/uuid1").accept("application/octet-stream").withToken())
            .andExpect(request().asyncStarted())
            .andDo(MvcResult::getAsyncResult)
            .andExpect(status().is2xxSuccessful)
            .andExpect(header().string("Content-Type", "application/octet-stream"))
            .andExpect(
                header().string(
                    "Content-Disposition",
                    "attachment; filename=\"=?UTF-8?Q?doc1?=\"; filename*=UTF-8''doc1"
                )
            )
            .andExpect(header().doesNotExist("Custom-Alfresco-Header"))
            .andExpect(content().bytes(ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes()))
    }

    @Test
    fun `approved premises documents data is returned`() {

        val res = mockMvc
            .perform(get("/ap-documents/X000010").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<List<Document>>()

        assertThat(res.size, equalTo(3))
        assertThat(res[0].author, equalTo("Dave Brown"))
        assertThat(res[0].id, equalTo("uuidap1"))
        assertThat(res[0].name, equalTo("ap_doc_1"))
        assertThat(res[1].author, equalTo("Brian Cox"))
        assertThat(res[1].id, equalTo("uuidap2"))
        assertThat(res[1].name, equalTo("ap_doc_2"))
        assertThat(res[2].author, equalTo("Steve Smith"))
        assertThat(res[2].id, equalTo("uuidap3"))
        assertThat(res[2].name, equalTo("ap_doc_3"))
    }
}
