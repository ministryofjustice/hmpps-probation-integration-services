package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.epf.*
import uk.gov.justice.digital.hmpps.epf.entity.Person
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `API call retuns a success response`() {
        val person = PersonGenerator.DEFAULT
        val crn = person.crn
        val eventNumber = 1
        val detailResponse = mockMvc
            .perform(get("/case-details/$crn/$eventNumber").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(detailResponse, equalTo(getDetailResponse()))
    }

    @ParameterizedTest
    @MethodSource("limitedAccess")
    fun `Response of Not Found when case is Restricted Or Excluded`(person: Person) {
        mockMvc
            .perform(get("/case-details/${person.crn}/1").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", equalTo("Person with crn of ${person.crn} not found")))
    }

    @Test
    fun `release details are correctly displayed`() {
        val person = PersonGenerator.RELEASED
        val crn = person.crn
        val eventNumber = 1
        val detailResponse = mockMvc
            .perform(get("/case-details/$crn/$eventNumber").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<CaseDetails>()

        assertThat(
            detailResponse, equalTo(
                CaseDetails(
                    null,
                    Name(person.forename, person.secondName, person.surname),
                    person.dateOfBirth,
                    person.gender.description,
                    Conviction(
                        SentenceGenerator.RELEASED_EVENT.convictionDate!!,
                        Court(SentenceGenerator.DEFAULT_COURT.name)
                    ),
                    Sentence(
                        SentenceGenerator.RELEASED_SENTENCE.date,
                        Court(SentenceGenerator.DEFAULT_COURT.name),
                        SentenceGenerator.RELEASE.date
                    ),
                    Provider(ProviderGenerator.DEFAULT.code, ProviderGenerator.DEFAULT.description),
                    null
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
            Conviction(SentenceGenerator.DEFAULT_EVENT.convictionDate!!, Court(SentenceGenerator.DEFAULT_COURT.name)),
            Sentence(
                SentenceGenerator.DEFAULT_SENTENCE.date,
                Court(SentenceGenerator.DEFAULT_COURT.name),
                null
            ),
            Provider(ProviderGenerator.DEFAULT.code, ProviderGenerator.DEFAULT.description),
            3
        )
    }

    companion object {
        @JvmStatic
        fun limitedAccess() = listOf(PersonGenerator.EXCLUDED, PersonGenerator.RESTRICTED)
    }
}
