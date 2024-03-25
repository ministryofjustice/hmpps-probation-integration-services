package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
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
import uk.gov.justice.digital.hmpps.api.model.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ApiIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @ParameterizedTest
    @MethodSource("caseIdentifiers")
    fun `successful retrieval of a case record by crn or noms id`(identifier: String, person: Person) {
        val record = mockMvc
            .perform(get("/case-records/$identifier").withToken())
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsJson<ProbationRecord>()

        val district = ProviderGenerator.DEFAULT_DISTRICT
        val team = ProviderGenerator.DEFAULT_TEAM
        val staff = ProviderGenerator.DEFAULT_STAFF
        assertThat(
            record,
            equalTo(
                ProbationRecord(
                    person.crn,
                    person.nomsId!!,
                    ReferenceDataGenerator.TIER_2.description,
                    Resourcing.NORMAL,
                    Manager(
                        Team(
                            team.code,
                            team.description,
                            LocalDeliveryUnit(district.code, district.description)
                        ),
                        ProviderGenerator.DEFAULT_STAFF.code,
                        Name(staff.forename, staff.surname),
                        "default.staff@moj.gov.uk"
                    ),
                    2,
                    false
                )
            )
        )
    }

    companion object {
        @JvmStatic
        fun caseIdentifiers() = listOf(
            Arguments.of(PersonGenerator.DEFAULT.crn, PersonGenerator.DEFAULT),
            Arguments.of(PersonGenerator.DEFAULT.nomsId, PersonGenerator.DEFAULT)
        )
    }
}
