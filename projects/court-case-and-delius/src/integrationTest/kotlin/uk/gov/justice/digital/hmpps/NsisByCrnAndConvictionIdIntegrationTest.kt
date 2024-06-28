package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.KeyValue
import uk.gov.justice.digital.hmpps.api.model.Nsi
import uk.gov.justice.digital.hmpps.api.model.NsiDetails
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator.BREACH_NSIS
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
//@TestPropertySource(properties = ["logging.level.org.hibernate.SQL=DEBUG", "logging.level.org.hibernate.orm.jdbc.bind=TRACE"])
internal class NsisByCrnAndConvictionIdIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        mockMvc
            .perform(get("/probation-case/$crn/convictions/1/nsis"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `request params not provided`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        mockMvc
            .perform(get("/probation-case/$crn/convictions/1/nsis").withToken())
            .andExpect(status().isBadRequest)
            .andExpect(status().reason("Required parameter 'nsiCodes' is not present."))
    }

    @Test
    fun `probation record not found`() {
        mockMvc
            .perform(get("/probation-case/A123456/convictions/123/nsis")
                .param("nsiCodes", "{}")
                .withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Person with crn of A123456 not found"))
    }

    @Test
    fun `sentence not found`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn

        mockMvc
            .perform(get("/probation-case/$crn/convictions/3/nsis")
                .param("nsiCodes", "{}")
                .withToken()
            )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Conviction with ID 3 for Offender with crn C123456 not found"))
    }

    @Test
    fun `return list of nsis`() {
        val crn = PersonGenerator.CURRENTLY_MANAGED.crn
        val event = SentenceGenerator.CURRENTLY_MANAGED

        val expectedResponse = NsiDetails(listOf(
                Nsi(BREACH_NSIS.id,
                    KeyValue(BREACH_NSIS.type.code, BREACH_NSIS.type.description
                )))
        )

        val response = mockMvc
            .perform(get("/probation-case/$crn/convictions/${event.id}/nsis")
                .param("nsiCodes", "NSI type")
                .withToken()
            )
            .andExpect(status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andReturn().response.contentAsJson<NsiDetails>()

        assertEquals(expectedResponse, response)
    }
}