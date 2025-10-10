package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.DEFAULT_PROVIDER
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EVENT
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integration.delius.entity.Person
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CaseDetailsIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `can retrieve basic case details`() {
        val person = PersonGenerator.DEFAULT
        val crn = person.crn
        val eventNumber = EVENT.number
        val detailResponse = mockMvc
            .perform(get("/case-details/$crn/$eventNumber").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(detailResponse, equalTo(getDetailResponse()))
    }

    @ParameterizedTest
    @MethodSource("limitedAccess")
    fun `Response includes lao info when case is Restricted Or Excluded`(person: Person, lad: LimitedAccessDetail) {
        val response = mockMvc
            .perform(get("/case-details/${person.crn}/1").withToken())
            .andExpect(status().isOk)
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(response.limitedAccess, equalTo(lad))
    }

    @Test
    fun `release details are correctly displayed`() {
        val person = PersonGenerator.WITH_RELEASE_DATE
        val crn = person.crn
        val eventNumber = 1
        val detailResponse = mockMvc
            .perform(get("/case-details/$crn/$eventNumber").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<CaseDetails>()

        MatcherAssert.assertThat(
            detailResponse, equalTo(
                CaseDetails(
                    null,
                    Name(person.forename, person.surname, person.secondName),
                    person.dateOfBirth,
                    person.gender.description,
                    Appearance(
                        SentenceGenerator.RELEASED_COURT_APPEARANCE.date.toLocalDate(),
                        Court(DataGenerator.COURT.name)
                    ),
                    SentenceSummary(
                        SentenceGenerator.RELEASE_DATE.date
                    ),
                    ResponsibleProvider(DEFAULT_PROVIDER.code, DEFAULT_PROVIDER.description),
                    null,
                    person.dynamicRsrScore,
                    null,
                )
            )
        )
    }

    private fun getDetailResponse(): CaseDetails {
        return CaseDetails(
            PersonGenerator.DEFAULT.nomsId,
            Name(
                PersonGenerator.DEFAULT.forename,
                PersonGenerator.DEFAULT.surname,
                PersonGenerator.DEFAULT.secondName,
            ),
            PersonGenerator.DEFAULT.dateOfBirth,
            PersonGenerator.DEFAULT.gender.description,
            Appearance(
                EVENT.courtAppearances.first().date.toLocalDate(),
                Court(DataGenerator.COURT.name)
            ),
            null,
            ResponsibleProvider(DEFAULT_PROVIDER.code, DEFAULT_PROVIDER.description),
            3,
            PersonGenerator.DEFAULT.dynamicRsrScore,
            null,
        )
    }

    companion object {
        @JvmStatic
        fun limitedAccess() = listOf(
            Arguments.of(
                PersonGenerator.EXCLUSION,
                LimitedAccessDetail(
                    excludedFrom = listOf(LimitedAccess.ExcludedFrom("Unknown")),
                    exclusionMessage = "There is an exclusion on this person",
                    restrictedTo = emptyList(),
                    restrictionMessage = null,
                )
            ),
            Arguments.of(
                PersonGenerator.RESTRICTION,
                LimitedAccessDetail(
                    excludedFrom = emptyList(),
                    exclusionMessage = null,
                    restrictedTo = listOf(
                        LimitedAccess.RestrictedTo("Unknown"),
                        LimitedAccess.RestrictedTo("john.smith@moj.gov.uk")
                    ),
                    restrictionMessage = "There is a restriction on this person",
                )
            ),
        )
    }
}
