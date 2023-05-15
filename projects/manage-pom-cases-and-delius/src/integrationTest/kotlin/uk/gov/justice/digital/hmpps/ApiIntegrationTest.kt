package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.api.model.Manager
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.api.model.Resourcing
import uk.gov.justice.digital.hmpps.api.model.Team
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ApiIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `successful retrieval of a case record by noms id`() {
        val person = PersonGenerator.DEFAULT
        val res = mockMvc
            .perform(get("/case-records/${person.nomsId}").withOAuth2Token(wireMockServer))
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val record = objectMapper.readValue<ProbationRecord>(res)
        val ldu = ProviderGenerator.DEFAULT_LDU
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
                            LocalDeliveryUnit(ldu.code, ldu.description)
                        ),
                        ProviderGenerator.DEFAULT_STAFF.code,
                        Name(staff.forename, staff.middleName, staff.surname),
                        "default.staff@moj.gov.uk"
                    ),
                    2
                )
            )
        )
    }
}
