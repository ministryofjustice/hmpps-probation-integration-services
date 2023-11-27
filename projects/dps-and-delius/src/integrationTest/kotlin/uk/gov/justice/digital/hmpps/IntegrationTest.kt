package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc

    @Autowired lateinit var wireMockServer: WireMockServer

    @Test
    fun `non-existent case returns 404`() {
        mockMvc
            .perform(get("/case/DOESNOTEXIST/documents").withOAuth2Token(wireMockServer))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get person and event level documents`() {
        mockMvc
            .perform(get("/case/${PersonGenerator.DEFAULT.nomisId}/documents").withOAuth2Token(wireMockServer))
            .andExpect(status().isOk)
            .andExpect(jsonPath("crn", equalTo(PersonGenerator.DEFAULT.crn)))
            .andExpect(jsonPath("name.forename", equalTo(PersonGenerator.DEFAULT.forename)))
            .andExpect(jsonPath("name.middleName", equalTo(PersonGenerator.DEFAULT.secondName)))
            .andExpect(jsonPath("name.surname", equalTo(PersonGenerator.DEFAULT.surname)))
            .andExpect(jsonPath("convictions[0].offence", equalTo("Burglary")))
            .andExpect(jsonPath("convictions[0].title", equalTo("Sentenced (6 Months)")))
            .andExpect(jsonPath("convictions[0].institutionName", equalTo("test institution")))
            .andExpect(jsonPath("convictions[0].documents[5].type", equalTo("Sentence related")))
            .andExpect(jsonPath("convictions[0].documents[4].type", equalTo("Crown Prosecution Service case pack")))
            .andExpect(jsonPath("convictions[0].documents[3].type", equalTo("Court Report")))
            .andExpect(jsonPath("convictions[0].documents[3].description", equalTo("court report type requested by test court on 01/01/2000")))
            .andExpect(jsonPath("convictions[0].documents[2].type", equalTo("Institutional Report")))
            .andExpect(jsonPath("convictions[0].documents[2].description", equalTo("institutional report type at test institution requested on 02/01/2000")))
            .andExpect(jsonPath("convictions[0].documents[1].type", equalTo("Contact related document")))
            .andExpect(jsonPath("convictions[0].documents[1].description", equalTo("Contact on 03/01/2000 for contact type")))
            .andExpect(jsonPath("convictions[0].documents[0].type", equalTo("Non Statutory Intervention related document")))
            .andExpect(jsonPath("convictions[0].documents[0].description", equalTo("Non Statutory Intervention for nsi type on 04/01/2000")))
            .andExpect(jsonPath("convictions[1].offence", equalTo("Daylight Robbery")))
            .andExpect(jsonPath("convictions[1].title", equalTo("Community Order")))
            .andExpect(jsonPath("convictions[1].active", equalTo(true)))
            .andExpect(jsonPath("documents[6].type", equalTo("Offender related")))
            .andExpect(jsonPath("documents[5].type", equalTo("PNC previous convictions")))
            .andExpect(jsonPath("documents[4].type", equalTo("Address assessment related document")))
            .andExpect(jsonPath("documents[3].type", equalTo("Personal contact related document")))
            .andExpect(jsonPath("documents[2].type", equalTo("Personal circumstance related document")))
            .andExpect(jsonPath("documents[1].type", equalTo("Contact related document")))
            .andExpect(jsonPath("documents[0].type", equalTo("Non Statutory Intervention related document")))
    }

    @Test
    fun `document can be downloaded`() {
        mockMvc.perform(get("/document/uuid1").accept("application/octet-stream").withOAuth2Token(wireMockServer))
            .andExpect(request().asyncStarted())
            .andDo(MvcResult::getAsyncResult)
            .andExpect(status().is2xxSuccessful)
            .andExpect(header().string("Content-Type", "application/octet-stream"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"=?UTF-8?Q?OFFENDER-related_document?=\"; filename*=UTF-8''OFFENDER-related%20document"))
            .andExpect(header().doesNotExist("Custom-Alfresco-Header"))
        // .andExpect(content().bytes(ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes()))
    }
}
