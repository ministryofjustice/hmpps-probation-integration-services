package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITHOUT_NOTES
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_1500_CHAR_NOTE
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_NOTES
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_NOTES_WITHOUT_ADDED_BY
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LIC_COND_MAIN_CAT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.ACTIVE_ORDER
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.EVENT_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.EVENT_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REQUIREMENT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REQUIREMENT_UNPAID_WORK
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

class SentencesIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unauthorized status returned`() {
        mockMvc.get("/sentences/X123456")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `no active sentences`() {
        val response = mockMvc.get("/sentences/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<MinimalSentenceOverview>()

        val expected = MinimalSentenceOverview(
            PersonDetailsGenerator.PERSONAL_DETAILS.toSummary()
        )

        assertEquals(expected, response)
    }

    @Test
    fun `get active sentences`() {
        val response = mockMvc.get("/sentences/${PersonGenerator.OVERVIEW.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<MinimalSentenceOverview>()

        val expected = MinimalSentenceOverview(
            PersonGenerator.OVERVIEW.toSummary(),
            listOf(
                MinimalSentence(
                    id = EVENT_2.id,
                    eventNumber = EVENT_2.eventNumber,
                    order = MinimalOrder(
                        description = "Pre-Sentence",
                        sentenceType = SentenceType.PRE_SENTENCE,
                    )
                ),
                MinimalSentence(
                    id = EVENT_1.id,
                    EVENT_1.eventNumber,
                    order = MinimalOrder(
                        ACTIVE_ORDER.type.description + " (12 Months)",
                        SentenceType.COMMUNITY,
                        ACTIVE_ORDER.date
                    ),
                    licenceConditions = listOf(
                        MinimalLicenceCondition(LC_WITH_NOTES.id, LIC_COND_MAIN_CAT.description, true),
                        MinimalLicenceCondition(LC_WITH_NOTES_WITHOUT_ADDED_BY.id, LIC_COND_MAIN_CAT.description, true),
                        MinimalLicenceCondition(LC_WITH_1500_CHAR_NOTE.id, LIC_COND_MAIN_CAT.description, true),
                        MinimalLicenceCondition(LC_WITHOUT_NOTES.id, LIC_COND_MAIN_CAT.description, true),
                    ),
                    requirements = listOf(
                        MinimalRequirement(REQUIREMENT_UNPAID_WORK.id, "Unpaid Work - Intensive", true),
                        MinimalRequirement(REQUIREMENT.id, "2 of 12 RAR days completed", true),
                    )
                )
            )
        )

        assertEquals(
            expected.copy(
                sentences = expected.sentences
                    .map {
                        it.copy(
                            licenceConditions = it.licenceConditions.sortedBy { lc -> lc.id },
                            requirements = it.requirements.sortedBy { r -> r.id }
                        )
                    }
                    .sortedBy { it.id }
            ),
            response.copy(
                sentences = response.sentences
                    .map {
                        it.copy(
                            licenceConditions = it.licenceConditions.sortedBy { lc -> lc.id },
                            requirements = it.requirements.sortedBy { r -> r.id }
                        )
                    }
                    .sortedBy { it.id }
            )
        )
    }

    @Test
    fun `sentence type is CUSTODY for a custodial sentence`() {
        val response = mockMvc.get("/sentences/${PersonGenerator.CUSTODY_PERSON.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<MinimalSentenceOverview>()

        val custodySentence = response.sentences.single { it.id == PersonGenerator.CUSTODY_EVENT.id }

        assertEquals(PersonGenerator.CUSTODY_EVENT.eventNumber, custodySentence.eventNumber)
        assertEquals("Custody Sentence Type", custodySentence.order?.description)
        assertEquals(SentenceType.CUSTODY, custodySentence.order?.sentenceType)
    }

    @Test
    fun `sentence type is pre-sentence for a pre-sentence sentence`() {
        val response = mockMvc.get("/sentences/${PersonGenerator.PRE_SENTENCE_PERSON.crn}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<MinimalSentenceOverview>()

        val expected = MinimalSentenceOverview(
            personSummary = PersonGenerator.PRE_SENTENCE_PERSON.toSummary(),
            sentences = listOf(
                MinimalSentence(
                    id = PersonGenerator.PRE_SENTENCE_EVENT.id,
                    eventNumber = PersonGenerator.PRE_SENTENCE_EVENT.eventNumber,
                    order = MinimalOrder(
                        description = "Pre-Sentence",
                        sentenceType = SentenceType.PRE_SENTENCE,
                    )
                )
            )
        )
        assertEquals(expected, response)
    }
}