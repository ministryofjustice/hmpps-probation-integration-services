package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.controller.model.CodeAndDescription
import uk.gov.justice.digital.hmpps.controller.model.Disability
import uk.gov.justice.digital.hmpps.controller.model.OffenderPersonalityDisorder
import uk.gov.justice.digital.hmpps.controller.model.PersonCircumstance
import uk.gov.justice.digital.hmpps.controller.model.PersonalDetailsAndCircumstances
import uk.gov.justice.digital.hmpps.data.generator.DisabilityGenerator
import uk.gov.justice.digital.hmpps.data.generator.NsiGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonalCircumstanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.andExpectJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class PersonDetailsAndCircumstancesIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `get person details returns full details`() {
        val crn = PersonGenerator.PERSON1.crn
        mockMvc.get("/case/$crn") { withToken() }
            .andExpect { status { isOk() } }
            .andExpectJson(
                PersonalDetailsAndCircumstances(
                    preferredLanguage = CodeAndDescription(
                        code = ReferenceDataGenerator.LANGUAGE_ENGLISH.code,
                        description = ReferenceDataGenerator.LANGUAGE_ENGLISH.description,
                    ),
                    personalCircumstances = listOf(
                        PersonCircumstance(
                            type = CodeAndDescription(
                                code = PersonalCircumstanceGenerator.TYPE_EMPLOYMENT.code,
                                description = PersonalCircumstanceGenerator.TYPE_EMPLOYMENT.description,
                            ),
                            subType = CodeAndDescription(
                                code = PersonalCircumstanceGenerator.SUB_TYPE_FULL_TIME.id.toString(),
                                description = PersonalCircumstanceGenerator.SUB_TYPE_FULL_TIME.description,
                            ),
                            updatedAt = PersonalCircumstanceGenerator.CIRCUMSTANCE.lastUpdated,
                        )
                    ),
                    disabilities = listOf(
                        Disability(
                            type = CodeAndDescription(
                                code = ReferenceDataGenerator.DISABILITY_TYPE_VISUAL.code,
                                description = ReferenceDataGenerator.DISABILITY_TYPE_VISUAL.description,
                            ),
                            updatedAt = DisabilityGenerator.DISABILITY.lastUpdated,
                        )
                    ),
                    offenderPersonalityDisorder = OffenderPersonalityDisorder(
                        status = CodeAndDescription(
                            code = NsiGenerator.OPD_STATUS.code,
                            description = NsiGenerator.OPD_STATUS.description,
                        )
                    ),
                )
            )
    }

    @Test
    fun `get person details returns empty lists when person has no circumstances or disabilities`() {
        val crn = PersonGenerator.PERSON2.crn
        mockMvc.get("/case/$crn") { withToken() }
            .andExpect { status { isOk() } }
            .andExpectJson(
                PersonalDetailsAndCircumstances(
                    preferredLanguage = null,
                    personalCircumstances = emptyList(),
                    disabilities = emptyList(),
                    offenderPersonalityDisorder = null,
                )
            )
    }

    @Test
    fun `get person details returns 404 when person not found`() {
        mockMvc.get("/case/Z999999") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `get person details returns 401 without token`() {
        val crn = PersonGenerator.PERSON1.crn
        mockMvc.get("/case/$crn")
            .andExpect { status { isUnauthorized() } }
    }
}

