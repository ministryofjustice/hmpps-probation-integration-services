package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
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
internal class PrisonerDocumentsIntTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val wireMockServer: WireMockServer
) {

    @Test
    fun `non-existent case returns 404`() {
        mockMvc.get("/probation-cases/DOESNOTEXIST/documents") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get person and event level documents`() {
        mockMvc.get("/probation-cases/${PersonGenerator.DEFAULT.nomisId}/documents") {
            withToken()
        }
            .andExpect {
                status { isOk() }

                jsonPath("crn", equalTo(PersonGenerator.DEFAULT.crn))
                jsonPath("name.forename", equalTo(PersonGenerator.DEFAULT.forename))
                jsonPath("name.middleName", equalTo(PersonGenerator.DEFAULT.secondName))
                jsonPath("name.surname", equalTo(PersonGenerator.DEFAULT.surname))

                jsonPath("convictions[0].offence", equalTo("Burglary"))
                jsonPath("convictions[0].title", equalTo("Sentenced (6 Months)"))
                jsonPath("convictions[0].institutionName", equalTo("test institution"))
                jsonPath("convictions[0].documents[5].type", equalTo("Sentence related"))
                jsonPath("convictions[0].documents[4].type", equalTo("Crown Prosecution Service case pack"))
                jsonPath("convictions[0].documents[3].type", equalTo("Court Report"))
                jsonPath(
                    "convictions[0].documents[3].description",
                    equalTo("court report type requested by test court on 01/01/2000")
                )
                jsonPath("convictions[0].documents[2].type", equalTo("Institutional Report"))
                jsonPath(
                    "convictions[0].documents[2].description",
                    equalTo("institutional report type at test institution requested on 02/01/2000")
                )
                jsonPath("convictions[0].documents[1].type", equalTo("Contact related document"))
                jsonPath(
                    "convictions[0].documents[1].description",
                    equalTo("Contact on 03/01/2000 for contact type")
                )
                jsonPath(
                    "convictions[0].documents[0].type",
                    equalTo("Non Statutory Intervention related document")
                )
                jsonPath(
                    "convictions[0].documents[0].description",
                    equalTo("Non Statutory Intervention for nsi type on 04/01/2000")
                )
                jsonPath("convictions[1].offence", equalTo("Daylight Robbery"))
                jsonPath("convictions[1].title", equalTo("Community Order"))
                jsonPath("convictions[1].active", equalTo(true))
                jsonPath("documents[6].type", equalTo("Offender related"))
                jsonPath("documents[5].type", equalTo("PNC previous convictions"))
                jsonPath("documents[4].type", equalTo("Address assessment related document"))
                jsonPath("documents[3].type", equalTo("Personal contact related document"))
                jsonPath("documents[2].type", equalTo("Personal circumstance related document"))
                jsonPath("documents[1].type", equalTo("Contact related document"))
                jsonPath("documents[0].type", equalTo("Non Statutory Intervention related document"))
            }
    }

    @Test
    fun `document can be downloaded`() {
        mockMvc.get("/document/00000000-0000-0000-0000-000000000001") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            withToken()
        }
            .andExpect {
                request { asyncStarted() }
            }
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
