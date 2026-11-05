package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.controller.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.controller.model.CommunityManager
import uk.gov.justice.digital.hmpps.controller.model.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.controller.model.Name
import uk.gov.justice.digital.hmpps.controller.model.OffenceAndSentenceDetail
import uk.gov.justice.digital.hmpps.data.generator.CommunityManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PersonSentenceIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `get person sentence returns relevant details`() {
        val personIdentifier = PersonGenerator.PERSON1.crn
        val offence = OffenceGenerator.OFFENCE1
        val disposal = DisposalGenerator.DISPOSAL

        mockMvc.get("/case/$personIdentifier/offence-and-sentence") { withToken() }
            .andExpect { status { isOk() } }
            .andExpectJson(
                OffenceAndSentenceDetail(
                    offence = CodeAndDescription(
                        offence.mainCategoryCode,
                        offence.mainCategoryDescription
                    ),
                    offenceSubCategory = CodeAndDescription(
                        offence.subCategoryCode,
                        offence.subCategoryDescription
                    ),
                    outcome = CodeAndDescription(disposal.disposalType.code, disposal.disposalType.description),
                    sentenceEndDate = disposal.enteredNotionalEndDate!!
                )
            )
    }

    @Test
    fun `get person sentence with nomis num returns relevant details`() {
        val personIdentifier = PersonGenerator.PERSON2.nomsNumber
        val offence = OffenceGenerator.OFFENCE1
        val disposal = DisposalGenerator.NOMS_DISPOSAL

        mockMvc.get("/case/$personIdentifier/offence-and-sentence") { withToken() }
            .andExpect { status { isOk() } }
            .andExpectJson(
                OffenceAndSentenceDetail(
                    offence = CodeAndDescription(
                        offence.mainCategoryCode,
                        offence.mainCategoryDescription
                    ),
                    offenceSubCategory = CodeAndDescription(
                        offence.subCategoryCode,
                        offence.subCategoryDescription
                    ),
                    outcome = CodeAndDescription(disposal.disposalType.code, disposal.disposalType.description),
                    sentenceEndDate = disposal.enteredNotionalEndDate!!
                )
            )
    }

    @Test
    fun `get community manager returns 404 when person not found`() {
        mockMvc.get("/case/Z999999/offence-and-sentence") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get community manager returns 404 when person with noms number not found`() {
        mockMvc.get("/case/A9999PP/offence-and-sentence") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get community manager returns 401 without token`() {
        val personIdentifier = PersonGenerator.PERSON1.crn
        mockMvc.get("/case/$personIdentifier/offence-and-sentence")
            .andExpect { status { isUnauthorized() } }
    }
}

