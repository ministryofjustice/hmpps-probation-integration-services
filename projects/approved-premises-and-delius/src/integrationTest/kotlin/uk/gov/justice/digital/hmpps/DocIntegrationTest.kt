package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DocIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    fun `document is downloaded`() {
        mockMvc.get("/documents/A000001/00000000-0000-0000-0000-000000000001") {
            withToken()
            accept = MediaType.APPLICATION_OCTET_STREAM
        }
            .andExpect { request { asyncStarted() } }
            .asyncDispatch()
            .andExpect {
                status { is2xxSuccessful() }

                header {
                    string("Content-Type", "application/octet-stream")
                    string(
                        "Content-Disposition",
                        "attachment; filename=\"=?UTF-8?Q?test.doc?=\"; filename*=UTF-8''test.doc"
                    )
                    doesNotExist("Custom-Alfresco-Header")
                }

                content {
                    bytes(
                        ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes()
                    )
                }
            }
    }

    @Test
    fun `list documents`() {
        mockMvc.get("/documents/${PersonGenerator.DEFAULT.crn}/all") {
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].id", equalTo("00000000-0000-0000-0000-000000000001"))
            jsonPath("$[0].level", equalTo("Conviction"))
            jsonPath("$[0].eventNumber", equalTo("8"))
            jsonPath("$[0].filename", equalTo("test.doc"))
            jsonPath("$[0].typeCode", equalTo("CONVICTION_DOCUMENT"))
            jsonPath("$[0].typeDescription", equalTo("Sentence related"))
            jsonPath("$[1].id", equalTo("00000000-0000-0000-0000-000000000002"))
            jsonPath("$[1].level", equalTo("Offender"))
            jsonPath("$[1].filename", equalTo("offender.doc"))
            jsonPath("$[1].typeCode", equalTo("OFFENDER_DOCUMENT"))
            jsonPath("$[1].typeDescription", equalTo("Offender related"))
        }
    }
}
