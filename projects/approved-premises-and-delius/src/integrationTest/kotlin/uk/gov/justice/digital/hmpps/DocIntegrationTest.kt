package uk.gov.justice.digital.hmpps

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
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DocIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `document is downloaded`() {
        mockMvc
            .perform(get("/documents/A000001/uuid1").accept("application/octet-stream").withToken())
            .andExpect(request().asyncStarted())
            .andDo(MvcResult::getAsyncResult)
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
        mockMvc
            .perform(get("/documents/${PersonGenerator.DEFAULT.crn}/all").withToken())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id", equalTo("uuid1")))
            .andExpect(jsonPath("$[0].level", equalTo("Conviction")))
            .andExpect(jsonPath("$[0].eventNumber", equalTo("8")))
            .andExpect(jsonPath("$[0].filename", equalTo("test.doc")))
            .andExpect(jsonPath("$[0].typeCode", equalTo("EVENT")))
            .andExpect(jsonPath("$[0].typeDescription", equalTo("Event")))
            .andExpect(jsonPath("$[1].id", equalTo("uuid2")))
            .andExpect(jsonPath("$[1].level", equalTo("Offender")))
            .andExpect(jsonPath("$[1].filename", equalTo("offender.doc")))
            .andExpect(jsonPath("$[1].typeCode", equalTo("PERSON")))
            .andExpect(jsonPath("$[1].typeDescription", equalTo("Person")))
    }
}
