package uk.gov.justice.digital.hmpps

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
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.epf.*
import uk.gov.justice.digital.hmpps.epf.entity.Person
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @Test
    fun `API call returns a success response`() {
        val person = PersonGenerator.DEFAULT
        val crn = person.crn
        val eventNumber = 1
        val detailResponse = mockMvc.get("/case-details/$crn/$eventNumber") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(detailResponse, equalTo(getDetailResponse()))
    }

    @ParameterizedTest
    @MethodSource("limitedAccess")
    fun `Response includes lao info when case is Restricted Or Excluded`(person: Person, lad: LimitedAccessDetail) {
        val response = mockMvc.get("/case-details/${person.crn}/1") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(response.limitedAccess, equalTo(lad))
    }

    @Test
    fun `release details are correctly displayed`() {
        val person = PersonGenerator.WITH_RELEASE_DATE
        val crn = person.crn
        val eventNumber = 1
        val detailResponse = mockMvc.get("/case-details/$crn/$eventNumber") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(
            detailResponse, equalTo(
                CaseDetails(
                    null,
                    Name(person.forename, person.secondName, person.surname),
                    person.dateOfBirth,
                    person.gender.description,
                    Appearance(
                        SentenceGenerator.DEFAULT_COURT_APPEARANCE.appearanceDate,
                        Court(SentenceGenerator.DEFAULT_COURT.name)
                    ),
                    Sentence(
                        SentenceGenerator.RELEASE_DATE.date
                    ),
                    Provider(ProviderGenerator.DEFAULT.code, ProviderGenerator.DEFAULT.description),
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
                PersonGenerator.DEFAULT.secondName,
                PersonGenerator.DEFAULT.surname
            ),
            PersonGenerator.DEFAULT.dateOfBirth,
            PersonGenerator.DEFAULT.gender.description,
            Appearance(
                SentenceGenerator.DEFAULT_COURT_APPEARANCE.appearanceDate,
                Court(SentenceGenerator.DEFAULT_COURT.name)
            ),
            null,
            Provider(ProviderGenerator.DEFAULT.code, ProviderGenerator.DEFAULT.description),
            3,
            PersonGenerator.DEFAULT.dynamicRsrScore,
            null,
        )
    }

    companion object {
        @JvmStatic
        fun limitedAccess() = listOf(
            Arguments.of(
                PersonGenerator.EXCLUDED,
                LimitedAccessDetail(
                    excludedFrom = listOf(LimitedAccess.ExcludedFrom("john.smith@moj.gov.uk")),
                    exclusionMessage = "This case is excluded!",
                    restrictedTo = emptyList(),
                    restrictionMessage = null,
                )
            ),
            Arguments.of(
                PersonGenerator.RESTRICTED,
                LimitedAccessDetail(
                    excludedFrom = emptyList(),
                    exclusionMessage = null,
                    restrictedTo = listOf(LimitedAccess.RestrictedTo("john.smith@moj.gov.uk")),
                    restrictionMessage = null,
                )
            ),
        )
    }
}
