package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.overview.Order
import uk.gov.justice.digital.hmpps.api.model.overview.Rar
import uk.gov.justice.digital.hmpps.api.model.sentence.*
import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator.COURT_DOCUMENT
import uk.gov.justice.digital.hmpps.data.generator.CourtReportGenerator.EVENT_DOCUMENT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SentenceIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `no active sentences`() {
        val response = mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<SentenceOverview>()

        val expected = SentenceOverview(
            PersonDetailsGenerator.PERSONAL_DETAILS.toSummary(), listOf(), ProbationHistory(0, null, 0, 0)
        )

        assertEquals(expected, response)
    }

    @Test
    fun `get active sentences`() {
        val response = mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/${PersonGenerator.OVERVIEW.crn}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<SentenceOverview>()

        val expected = SentenceOverview(
            PersonGenerator.OVERVIEW.toSummary(),
            listOf(
                Sentence(
                    OffenceDetails(
                        "1234567",
                        Offence("Another Murder", 1),
                        LocalDate.now(),
                        "overview",
                        emptyList()
                    ),
                    Conviction(null, null, null, listOf()),
                    null,
                    listOf(),
                    listOf(),
                    null,
                    listOf()
                ),
                Sentence(
                    OffenceDetails(
                        "7654321",
                        Offence("Murder", 1),
                        LocalDate.now(),
                        "overview",
                        listOf(
                            Offence("Burglary", 1),
                            Offence("Assault", 1)
                        )
                    ),
                    Conviction(
                        "Hull Court",
                        "Birmingham Court",
                        LocalDate.now(),
                        listOf(AdditionalSentence(3, null, null, "Disqualified from Driving"))
                    ),
                    Order("Default Sentence Type", 12, null, LocalDate.now().minusDays(14)),
                    listOf(
                        Requirement(
                            "F",
                            LocalDate.now().minusDays(1),
                            LocalDate.now(),
                            LocalDate.now().minusDays(2),
                            LocalDate.now().minusDays(3),
                            null,
                            "1 days RAR, 1 completed",
                            12,
                            null,
                            "my notes",
                            Rar(completed = 1, scheduled = 0, totalDays = 1)
                        ),
                        Requirement(
                            "W",
                            LocalDate.now().minusDays(1),
                            LocalDate.now(),
                            LocalDate.now().minusDays(2),
                            LocalDate.now().minusDays(3),
                            null,
                            "Unpaid Work - Intensive",
                            12,
                            null,
                            "my notes",
                            null
                        )
                    ),
                    listOf(
                        CourtDocument(COURT_DOCUMENT.alfrescoId, LocalDate.now().minusDays(1), "court report"),
                        CourtDocument(EVENT_DOCUMENT.alfrescoId, LocalDate.now().minusDays(3), "event report")
                    ),
                    "3 minutes completed (of 12 hours)",
                    listOf(
                        LicenceCondition(
                            "lic cond main",
                            "Lic Sub cat",
                            LocalDate.now().minusDays(7),
                            LocalDate.now(),
                            listOf(
                                LicenceConditionNote(
                                    "Comment added by CVL Service on 22/04/2024 at 10:00",
                                    """
                                        Licence Condition created automatically from the Create and Vary a licence system of
                                        Allow person(s) as designated by your supervising officer to install an electronic monitoring tag on you and access to install any associated equipment in your property, and for the purpose of ensuring that equipment is functioning correctly. You must not damage or tamper with these devices and ensure that the tag is charged, and report to your supervising officer and the EM provider immediately if the tag or the associated equipment are not working correctly. This will be for the purpose of monitoring your alcohol abstinence licence condition(s) unless otherwise authorised by your supervising officer.
                                        
                                    """.trimIndent(),
                                    false
                                    ),
                                LicenceConditionNote(
                                    "Comment added by Joe Root on 23/04/2024 at 13:45",
                                    """
                                        You must not drink any alcohol until Wednesday 7th August 2024 unless your
                                        probation officer says you can. You will need to wear an electronic tag all the time so
                                        we can check this.
                                    """.trimIndent(),
                                    false
                                )
                            )
                            ),
                        LicenceCondition(
                            "lic cond main",
                            imposedReleasedDate = LocalDate.now().minusDays(14),
                        )
                    )
                )
            ),
            ProbationHistory(2, LocalDate.now().minusDays(7), 2, 2)
        )

        assertEquals(expected, response)
    }

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/sentence/X123456"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }
}