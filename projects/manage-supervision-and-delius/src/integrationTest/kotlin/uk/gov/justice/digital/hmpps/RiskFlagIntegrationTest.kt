package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.risk.PersonRiskFlag
import uk.gov.justice.digital.hmpps.api.model.risk.PersonRiskFlags
import uk.gov.justice.digital.hmpps.api.model.risk.RiskLevel
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.DEREGISTRATION_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.MAPPA_LEVEL
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.OVERVIEW
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.PERSON_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISTRATION_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISTRATION_3
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.REGISTRATION_REVIEW_2
import uk.gov.justice.digital.hmpps.service.toRiskFlag
import uk.gov.justice.digital.hmpps.service.toRiskFlagRemoval
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

class RiskFlagIntegrationTest: IntegrationTestBase() {

    @Test
    fun `all risk flags are returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/risk-flags/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonRiskFlags>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.mappa?.level, equalTo(2))
        assertThat(res.mappa?.category, equalTo(0))
        assertThat(res.opd?.eligible, equalTo(true))
        assertThat(res.riskFlags.size, equalTo(3))
        assertThat(res.riskFlags[0].level, equalTo(RiskLevel.HIGH))
        assertThat(res.riskFlags[0].description, equalTo(REGISTRATION_2.type.description))
        assertThat(res.riskFlags[0].mostRecentReviewDate, equalTo(REGISTRATION_REVIEW_2.date))
        assertThat(res.riskFlags[0].levelCode, equalTo(null))
        assertThat(res.riskFlags[0].levelDescription, equalTo(null))
        assertThat(res.riskFlags[0].riskNotes!![0].note, equalTo("Risk Notes 2"))
        assertFalse(res.riskFlags[0].riskNotes!![0].hasNoteBeenTruncated!!)
        assertThat(res.riskFlags[1].level, equalTo(RiskLevel.LOW))
        assertThat(res.riskFlags[1].levelCode, equalTo(MAPPA_LEVEL.code))
        assertThat(res.riskFlags[1].levelDescription, equalTo(MAPPA_LEVEL.description))
        assertThat(res.riskFlags[2].level, equalTo(RiskLevel.INFORMATION_ONLY))
        assertThat(res.removedRiskFlags.size, equalTo(1))
        assertThat(
            res.removedRiskFlags[0], equalTo(
                REGISTRATION_3.toRiskFlag().copy(
                    mostRecentReviewDate = REGISTRATION_REVIEW_2.date, removalHistory = listOf(
                        DEREGISTRATION_1.toRiskFlagRemoval()
                    )
                )
            )
        )
        assertThat(res.removedRiskFlags[0].removalHistory[0].riskRemovalNotes!!.size, equalTo(2))
        assertThat(res.removedRiskFlags[0].removalHistory[0].riskRemovalNotes!![0].note, equalTo("Made a mistake"))
        assertFalse(res.removedRiskFlags[0].removalHistory[0].riskRemovalNotes!![0].hasNoteBeenTruncated!!)
    }

    @Test
    fun `opd and mappa is not returned`() {
        val person = PERSON_2
        val res = mockMvc
            .perform(get("/risk-flags/${person.crn}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonRiskFlags>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.mappa, equalTo(null))
        assertThat(res.opd, equalTo(null))
    }

    @Test
    fun `individual risk flag is returned`() {

        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/risk-flags/${person.crn}/${REGISTRATION_2.id}").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonRiskFlag>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.riskFlag.description, equalTo(REGISTRATION_2.type.description))
        assertThat(res.riskFlag.mostRecentReviewDate, equalTo(REGISTRATION_REVIEW_2.date))
    }

    @Test
    fun `individual risk flag is returned with single note`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/risk-flags/${person.crn}/${REGISTRATION_2.id}/note/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonRiskFlag>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.riskFlag.description, equalTo(REGISTRATION_2.type.description))
        assertThat(res.riskFlag.mostRecentReviewDate, equalTo(REGISTRATION_REVIEW_2.date))
        assertThat(res.riskFlag.riskNote!!.note, equalTo("Risk Notes 1" + System.lineSeparator()))
    }

    @Test
    fun `individual risk flag is returned with removal history note`() {
        val person = OVERVIEW
        val res = mockMvc
            .perform(get("/risk-flags/${person.crn}/${REGISTRATION_3.id}/risk-removal-note/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<PersonRiskFlag>()
        assertThat(res.personSummary.crn, equalTo(person.crn))
        assertThat(res.riskFlag.description, equalTo(REGISTRATION_3.type.description))
        assertNull(res.riskFlag.removalHistory[0].riskRemovalNotes)
        assertThat(
            res.riskFlag.removalHistory[0].riskRemovalNote,
            equalTo(
                NoteDetail(
                    1,
                    "Alan Shearer",
                    LocalDate.of(2024, 4, 23),
                    "My note"
                )
            )
        )
    }

    @Test
    fun `individual risk flag not found`() {

        val person = OVERVIEW
        mockMvc.perform(get("/risk-flags/${person.crn}/9999999").withToken()).andExpect(status().isNotFound)
    }
}
