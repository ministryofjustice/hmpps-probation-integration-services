package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.api.proxy.CompareReport
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class ProxyIntegrationTest {

    @SpyBean
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var featureFlags: FeatureFlags

    @BeforeEach
    fun setup() {
        whenever(featureFlags.enabled("ccd-offender-detail-enabled")).thenReturn(false)
        whenever(featureFlags.enabled("ccd-offender-summary-enabled")).thenReturn(false)
        whenever(featureFlags.enabled("ccd-offender-managers-enabled")).thenReturn(false)
    }

    @Test
    fun `proxies to community api for offenders all`() {
        mockMvc
            .perform(get("/secure/offenders/crn/C123456/all").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.otherIds.crn").value("C123456"))
    }

    @Test
    fun `proxies to community api for offenders allOffenderManagers`() {
        mockMvc
            .perform(get("/secure/offenders/crn/C123456/allOffenderManagers?includeProbationAreaTeams=true").withToken())
            .andExpect(status().is2xxSuccessful)
    }

    @Test
    fun `proxies to community api for offenders summary`() {
        mockMvc
            .perform(get("/secure/offenders/crn/C123456").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.contactDetails.addresses[0].town").value("Leicester"))
    }

    @Test
    fun `proxies to community api with error`() {
        mockMvc
            .perform(get("/secure/offenders/crn/CRNXXX/all").withToken())
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.developerMessage").value("Offender with CRN 'CRNXXX' not found"))
    }

    @Test
    fun `compare new endpoints with community api endpoints`() {

        val forCompare = ResourceUtils.getFile("classpath:simulations/__files/forCompare.json")
            .inputStream().readBytes().toString(Charsets.UTF_8)

        doReturn(forCompare).`when`(mapper).writeValueAsString(any())
        val res = mockMvc.perform(
            post("/secure/compare")
                .contentType("application/json;charset=utf-8")
                .content(
                    """
                    {
                        "params": {
                            "crn": "C123456"
                        },
                        "uri": "OFFENDER_DETAIL"
                    }
                """
                )
                .withToken()
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsJson<CompareReport>()

        assertThat(res.endPointName, equalTo("OFFENDER_DETAIL"))
        assertThat(res.issues?.size, equalTo(6))
    }

    @Test
    fun `compare new endpoints with community api endpoints multiple parameters`() {

        val res = mockMvc.perform(
            post("/secure/compare")
                .contentType("application/json;charset=utf-8")
                .content(
                    """
                    {
                        "params": {
                            "crn": "C123456",
                            "convictionId": ${SentenceGenerator.CURRENTLY_MANAGED.id},
                            "activeOnly": true,
                            "excludeSoftDeleted": true
                        },
                        "uri": "CONVICTION_REQUIREMENTS"
                    }
                """
                )
                .withToken()
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsJson<CompareReport>()

        assertThat(res.endPointName, equalTo("CONVICTION_REQUIREMENTS"))
        assertThat(res.success, equalTo(false))
    }

    @Test
    fun `compare new endpoints with community api endpoints with json arrays`() {

        val res = mockMvc.perform(
            post("/secure/compare")
                .contentType("application/json;charset=utf-8")
                .content(
                    """
                    {
                        "params": {"crn": "C123456", "includeProbationAreaTeams": false },
                        "uri": "OFFENDER_MANAGERS"
                    }
                """
                )
                .withToken()
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsJson<CompareReport>()

        assertThat(res.endPointName, equalTo("OFFENDER_MANAGERS"))
        assertThat(res.success, equalTo(false))
    }

    @Test
    fun `compare multiple endpoints with mutliple crns and multiple parameters`() {
        val res = mockMvc.perform(
            post("/secure/compareAll")
                .contentType("application/json;charset=utf-8")
                .content(
                    """
                    {
                        "crns": [ "C123456", "C123456"],
                        "uriConfig": {
                            "OFFENDER_DETAIL": {},
                            "OFFENDER_MANAGERS": {
                                "includeProbationAreaTeams": false
                            },
                            "CONVICTIONS": {
                                "activeOnly": false
                            },
                            "CONVICTION_REQUIREMENTS": {
                                "convictionId": ${SentenceGenerator.CURRENTLY_MANAGED.id},
                                "activeOnly": true,
                                "excludeSoftDeleted": true
                            }
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsJson<List<CompareReport>>()

        assertThat(res.size, equalTo(8))
    }
}
