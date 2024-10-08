package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.conviction.*
import uk.gov.justice.digital.hmpps.data.generator.AdditionalSentenceGenerator.SENTENCE_DISQ
import uk.gov.justice.digital.hmpps.data.generator.CourtGenerator.BHAM
import uk.gov.justice.digital.hmpps.data.generator.CourtGenerator.PROBATION_AREA
import uk.gov.justice.digital.hmpps.data.generator.DisposalTypeGenerator.CURFEW_ORDER
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator.WSIHMP
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.ADDITIONAL_OFFENCE
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.COURT_APPEARANCE
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENT_ORDER_MANAGER
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.CURRENT_SENTENCE
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.MAIN_OFFENCE
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator.ALLOCATED
import uk.gov.justice.digital.hmpps.data.generator.UnpaidWorkGenerator.UNPAID_WORK_DETAILS_1
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ConvictionByCrnIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        mockMvc
            .perform(get("/probation-case/$crn/convictions"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `API call probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456/convictions").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.developerMessage").value("Person with crn of A123456 not found"))
    }

    @Test
    fun `return a list of active events for a person`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.CURRENTLY_MANAGED
        val mainOffence = SentenceGenerator.MAIN_OFFENCE_DEFAULT
        val additionalOffence = SentenceGenerator.ADDITIONAL_OFFENCE_DEFAULT

        val expectedMainOffenceDetail =
            OffenceDetail(
                MAIN_OFFENCE.code,
                MAIN_OFFENCE.description,
                MAIN_OFFENCE.abbreviation,
                MAIN_OFFENCE.mainCategoryCode,
                MAIN_OFFENCE.mainCategoryDescription,
                MAIN_OFFENCE.mainCategoryAbbreviation,
                MAIN_OFFENCE.ogrsOffenceCategory.description,
                MAIN_OFFENCE.subCategoryCode,
                MAIN_OFFENCE.subCategoryDescription,
                MAIN_OFFENCE.form20Code,
                MAIN_OFFENCE.subCategoryAbbreviation,
                MAIN_OFFENCE.cjitCode
            )
        val expectedAdditionalOffenceDetail =
            OffenceDetail(
                ADDITIONAL_OFFENCE.code,
                ADDITIONAL_OFFENCE.description,
                ADDITIONAL_OFFENCE.abbreviation,
                ADDITIONAL_OFFENCE.mainCategoryCode,
                ADDITIONAL_OFFENCE.mainCategoryDescription,
                ADDITIONAL_OFFENCE.mainCategoryAbbreviation,
                ADDITIONAL_OFFENCE.ogrsOffenceCategory.description,
                ADDITIONAL_OFFENCE.subCategoryCode,
                ADDITIONAL_OFFENCE.subCategoryDescription,
                ADDITIONAL_OFFENCE.form20Code,
                ADDITIONAL_OFFENCE.subCategoryAbbreviation,
                ADDITIONAL_OFFENCE.cjitCode
            )
        val expectedOffences = listOf(
            Offence(
                "M${mainOffence.id}",
                mainOffence = true,
                expectedMainOffenceDetail,
                mainOffence.date,
                mainOffence.offenceCount,
                mainOffence.tics,
                mainOffence.verdict,
                mainOffence.offenderId,
                mainOffence.created.toLocalDateTime(),
                mainOffence.updated.toLocalDateTime()
            ),
            Offence(
                "A${additionalOffence.id}",
                mainOffence = false,
                expectedAdditionalOffenceDetail,
                additionalOffence.date,
                additionalOffence.offenceCount,
                tics = null,
                verdict = null,
                PersonGenerator.CURRENTLY_MANAGED.id,
                additionalOffence.created.toLocalDateTime(),
                additionalOffence.updated.toLocalDateTime()
            )
        )
        val expectedSentence = Sentence(
            CURRENT_SENTENCE.id,
            CURRENT_SENTENCE.disposalType.description,
            CURRENT_SENTENCE.entryLength,
            CURRENT_SENTENCE.entryLengthUnit?.description,
            CURRENT_SENTENCE.length2,
            CURRENT_SENTENCE.entryLength2Unit?.description,
            CURRENT_SENTENCE.length,
            CURRENT_SENTENCE.effectiveLength,
            CURRENT_SENTENCE.lengthInDays,
            CURRENT_SENTENCE.enteredSentenceEndDate,
            UnpaidWork(
                UNPAID_WORK_DETAILS_1.upwLengthMinutes, 7,
                Appointments(7, 3, 2, 1, 1),
                ReferenceDataGenerator.HOURS_WORKED.description
            ),
            CURRENT_SENTENCE.startDate,
            sentenceType = KeyValue(CURFEW_ORDER.sentenceType, CURFEW_ORDER.description),
            additionalSentences = listOf(
                AdditionalSentence(
                    SENTENCE_DISQ.id,
                    KeyValue(SENTENCE_DISQ.type.code, SENTENCE_DISQ.type.description),
                    SENTENCE_DISQ.amount,
                    SENTENCE_DISQ.length,
                    SENTENCE_DISQ.notes
                )
            ),
            failureToComplyLimit = CURFEW_ORDER.failureToComplyLimit,
            cja2003Order = CURFEW_ORDER.cja2003Order,
            legacyOrder = CURFEW_ORDER.legacyOrder
        )
        val expectedResponse = listOf(
            Conviction(
                event.id, event.eventNumber,
                event.active,
                event.inBreach,
                2,
                event.breachEnd,
                false,
                event.convictionDate,
                event.referralDate,
                expectedOffences,
                expectedSentence,
                KeyValue("101", "Adjourned - Pre-Sentence Report"),
                Custody(
                    "FD1234",
                    Institution(
                        WSIHMP.id.institutionId,
                        WSIHMP.id.establishment,
                        WSIHMP.code,
                        WSIHMP.description,
                        WSIHMP.institutionName,
                        WSIHMP.establishmentType?.let { KeyValue(it.code, it.description) },
                        WSIHMP.private,
                        WSIHMP.nomisCdeCode
                    ),
                    CustodyRelatedKeyDates(
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(2),
                        LocalDate.now().plusDays(3),
                        LocalDate.now().plusDays(4),
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(6),
                        LocalDate.now().plusDays(7),
                        LocalDate.now().plusDays(8),
                    ),
                    KeyValue(
                        ReferenceDataGenerator.CUSTODIAL_STATUS.code,
                        ReferenceDataGenerator.CUSTODIAL_STATUS.description
                    ),
                    LocalDate.now()
                ),
                Court(
                    BHAM.id,
                    BHAM.code,
                    BHAM.selectable,
                    BHAM.courtName,
                    BHAM.telephoneNumber,
                    BHAM.faxNumber,
                    BHAM.buildingName,
                    BHAM.street,
                    BHAM.locality,
                    BHAM.town,
                    BHAM.county,
                    BHAM.postcode,
                    BHAM.country,
                    BHAM.courtTypeId,
                    BHAM.createdDatetime.toLocalDateTime(),
                    BHAM.lastUpdatedDatetime.toLocalDateTime(),
                    BHAM.probationAreaId,
                    BHAM.secureEmailAddress,
                    KeyValue(BHAM.probationArea.code, BHAM.probationArea.description),
                    KeyValue(BHAM.courtType.code, BHAM.courtType.description)
                ),
                CourtAppearanceBasic(
                    COURT_APPEARANCE.id,
                    COURT_APPEARANCE.appearanceDate,
                    COURT_APPEARANCE.court.code,
                    COURT_APPEARANCE.court.courtName,
                    KeyValue(COURT_APPEARANCE.appearanceType.code, COURT_APPEARANCE.appearanceType.description),
                    COURT_APPEARANCE.person.crn
                ),
                listOf(
                    OrderManager(
                        PROBATION_AREA.id,
                        null,
                        CURRENT_ORDER_MANAGER.id,
                        ALLOCATED.getName(),
                        ALLOCATED.code,
                        CURRENT_ORDER_MANAGER.allocationDate.toLocalDateTime(),
                        CURRENT_ORDER_MANAGER.endDate?.toLocalDateTime(),
                        null,
                        null,
                        PROBATION_AREA.code
                    )
                )
            )
        )

        val response = mockMvc
            .perform(get("/probation-case/$crn/convictions?activeOnly=true").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<List<Conviction>>()

        assertEquals(expectedResponse.first().convictionId, response.first().convictionId)
    }

    @Test
    fun `return a list of active and inactive events for a person`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        mockMvc
            .perform(get("/probation-case/$crn/convictions").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[1].active").value(true))
            .andExpect(jsonPath("$[0].active").value(false))
    }

    @Test
    fun `return a empty list for a person with no active events`() {
        val crn = PersonGenerator.NO_ACTIVE_EVENTS.crn
        mockMvc
            .perform(get("/probation-case/$crn/convictions?activeOnly=true").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `convictions record soft deleted return emptyList for convictions`() {
        val response = mockMvc
            .perform(get("/probation-case/S123456/convictions").withToken())
            .andExpect(status().isOk).andReturn()
            .response.contentAsJson<List<Conviction>>()
        MatcherAssert.assertThat(response, Matchers.equalTo(emptyList()))
    }
}