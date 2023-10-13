package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DocIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `document is downloaded`() {
        mockMvc.perform(
            get("/documents/A000001/uuid1").accept("application/octet-stream").withOAuth2Token(wireMockserver)
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(header().string("Content-Type", "application/octet-stream"))
            .andExpect(
                header().string(
                    "Content-Disposition",
                    "attachment; filename=\"=?UTF-8?Q?test.doc?=\"; filename*=UTF-8''test.doc"
                )
            )
            .andExpect(header().doesNotExist("Custom-Alfresco-Header"))
            .andExpect(content().bytes(ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes()))
    }

    @Test
    fun `list documents`() {
        mockMvc.perform(get("/documents/${PersonGenerator.DEFAULT.crn}/all").withOAuth2Token(wireMockserver))
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.equalTo("uuid1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].level", Matchers.equalTo("Conviction")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventNumber", Matchers.equalTo("8")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].filename", Matchers.equalTo("test.doc")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].typeCode", Matchers.equalTo("EVENT")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].typeDescription", Matchers.equalTo("Event")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.equalTo("uuid2")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].level", Matchers.equalTo("Offender")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].filename", Matchers.equalTo("offender.doc")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].typeCode", Matchers.equalTo("PERSON")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].typeDescription", Matchers.equalTo("Person")))
    }
}
