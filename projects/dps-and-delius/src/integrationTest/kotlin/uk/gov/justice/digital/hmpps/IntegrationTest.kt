package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
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
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wireMockServer: WireMockServer
) {

    @Test
    fun `non-existent case returns 404`() {
        mockMvc.get("/case/DOESNOTEXIST/documents") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get person and event level documents`() {
        mockMvc.get("/case/${PersonGenerator.DEFAULT.nomisId}/documents") { withToken() }
            .andExpect {
                status { isOk() }

                jsonPath("crn") { value(equalTo(PersonGenerator.DEFAULT.crn)) }

                jsonPath("name.forename") { value(equalTo(PersonGenerator.DEFAULT.forename)) }
                jsonPath("name.middleName") { value(equalTo(PersonGenerator.DEFAULT.secondName)) }
                jsonPath("name.surname") { value(equalTo(PersonGenerator.DEFAULT.surname)) }

                jsonPath("convictions[0].offence") { value(equalTo("Burglary")) }
                jsonPath("convictions[0].title") { value(equalTo("Sentenced (6 Months)")) }
                jsonPath("convictions[0].institutionName") { value(equalTo("test institution")) }
                jsonPath("convictions[0].documents[5].type") { value(equalTo("Sentence related")) }
                jsonPath("convictions[0].documents[4].type") { value(equalTo("Crown Prosecution Service case pack")) }
                jsonPath("convictions[0].documents[3].type") { value(equalTo("Court Report")) }
                jsonPath("convictions[0].documents[3].description") { value(equalTo("court report type requested by test court on 01/01/2000")) }
                jsonPath("convictions[0].documents[2].type") { value(equalTo("Institutional Report")) }
                jsonPath("convictions[0].documents[2].description") { value(equalTo("institutional report type at test institution requested on 02/01/2000")) }
                jsonPath("convictions[0].documents[1].type") { value(equalTo("Contact related document")) }
                jsonPath("convictions[0].documents[1].description") { value(equalTo("Contact on 03/01/2000 for contact type")) }
                jsonPath("convictions[0].documents[0].type") { value(equalTo("Non Statutory Intervention related document")) }
                jsonPath("convictions[0].documents[0].description") { value(equalTo("Non Statutory Intervention for nsi type on 04/01/2000")) }
                jsonPath("convictions[1].offence") { value(equalTo("Daylight Robbery")) }
                jsonPath("convictions[1].title") { value(equalTo("Community Order")) }
                jsonPath("convictions[1].active") { value(equalTo(true)) }

                jsonPath("documents[6].type") { value(equalTo("Offender related")) }
                jsonPath("documents[5].type") { value(equalTo("PNC previous convictions")) }
                jsonPath("documents[4].type") { value(equalTo("Address assessment related document")) }
                jsonPath("documents[3].type") { value(equalTo("Personal contact related document")) }
                jsonPath("documents[2].type") { value(equalTo("Personal circumstance related document")) }
                jsonPath("documents[1].type") { value(equalTo("Contact related document")) }
                jsonPath("documents[0].type") { value(equalTo("Non Statutory Intervention related document")) }
            }
    }

    @Test
    fun `document can be downloaded`() {
        mockMvc.get("/document/00000000-0000-0000-0000-000000000001") {
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
                        "attachment; filename=\"=?UTF-8?Q?OFFENDER-related_document?=\"; filename*=UTF-8''OFFENDER-related%20document"
                    )
                    doesNotExist("Custom-Alfresco-Header")
                }

                content {
                    bytes(ResourceUtils.getFile("classpath:simulations/__files/document.pdf").readBytes())
                }
            }
    }
}
