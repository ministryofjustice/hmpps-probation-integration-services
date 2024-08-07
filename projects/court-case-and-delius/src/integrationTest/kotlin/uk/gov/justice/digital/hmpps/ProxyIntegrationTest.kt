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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.ResourceUtils
import uk.gov.justice.digital.hmpps.api.proxy.CompareAllReport
import uk.gov.justice.digital.hmpps.api.proxy.CompareReport
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator.NSI_TYPE
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@TestPropertySource(properties = ["lao-access.ignore-exclusions = false", "lao-access.ignore-restrictions = true"])
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
    fun `compare new endpoints with community api endpoints for pss`() {

        val res = mockMvc.perform(
            post("/secure/compare")
                .contentType("application/json;charset=utf-8")
                .content(
                    """
                    {
                        "params": {
                            "crn": "C123456",
                            "convictionId": ${SentenceGenerator.CURRENTLY_MANAGED.id}
                        },
                        "uri": "CONVICTION_BY_ID_PSS"
                    }
                """
                )
                .withToken()
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsJson<CompareReport>()

        assertThat(res.endPointName, equalTo("CONVICTION_BY_ID_PSS"))
        assertThat(res.success, equalTo(true))
    }

    @Test
    fun `compare new endpoints with community api endpoints with an array of string as a parameter`() {

        val res = mockMvc.perform(
            post("/secure/compare")
                .contentType("application/json;charset=utf-8")
                .content(
                    """
                    {
                        "params": {
                            "crn": "C123456",
                            "convictionId": ${SentenceGenerator.CURRENTLY_MANAGED.id},
                            "nsiCodes": "${NSI_TYPE.code},${NSI_TYPE.code}"
                        },
                        "uri": "CONVICTION_BY_ID_NSIS"
                    }
                """
                )
                .withToken()
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsJson<CompareReport>()

        assertThat(res.endPointName, equalTo("CONVICTION_BY_ID_NSIS"))
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
                        "pageNumber": 1,
                        "pageSize": 1,
                        "crns": [ "C123456", "P123456"],
                        "uriConfig": {
                            "OFFENDER_DETAIL": {},
                            "OFFENDER_SUMMARY": {},
                            "OFFENDER_MANAGERS": {
                                "includeProbationAreaTeams": false
                            },
                            "CONVICTIONS": {
                                "activeOnly": false
                            },
                            "CONVICTION_BY_ID": {
                                "convictionId": "?"
                            },
                            "CONVICTION_REQUIREMENTS": {
                                "convictionId": "?",
                                "activeOnly": true,
                                "excludeSoftDeleted": true
                            },
                            "CONVICTION_BY_ID_ATTENDANCES": {
                                "convictionId": "?",
                                "activeOnly": true
                            },
                            "CONVICTION_BY_ID_NSIS": {
                                "convictionId": "?",
                                "nsiCodes": "?"
                            },
                            "CONVICTION_BY_NSIS_ID": {
                                "convictionId": "?",
                                "nsiId": "?"
                            },
                            "CONVICTION_BY_ID_PSS": {
                                "convictionId": "?"
                            },
                            "CONVICTION_BY_ID_COURT_APPEARANCES": {
                                "convictionId": "?",
                                "activeOnly": true
                            },
                            "CONVICTION_BY_ID_COURT_REPORTS": {
                                "convictionId": "?",
                                "activeOnly": true
                            }
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsJson<CompareAllReport>()

        assertThat(res.totalNumberOfRequests, equalTo(12))
        assertThat(res.totalNumberOfCrns, equalTo(2))
        assertThat(res.currentPageNumber, equalTo(1))
    }

    @Test
    fun `compare when test data is not available`() {
        val res = mockMvc.perform(
            post("/secure/compareAll")
                .contentType("application/json;charset=utf-8")
                .content(
                    """
                    {
                        "pageNumber": 2,
                        "pageSize": 1,
                        "crns": [ "C123456", "U123456"],
                        "uriConfig": {
                            "OFFENDER_DETAIL": {},
                            "OFFENDER_SUMMARY": {},
                            "CONVICTION_BY_ID": {
                                "convictionId": "?"
                            },
                            "CONVICTION_REQUIREMENTS": {
                                "convictionId": "?",
                                "activeOnly": true,
                                "excludeSoftDeleted": true
                            },
                            "CONVICTION_BY_ID_NSIS": {
                                "convictionId": "?",
                                "nsiCodes": "?"
                            },
                            "CONVICTION_BY_ID_PSS": {
                                "convictionId": "?"
                            }
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().is2xxSuccessful).andReturn().response.contentAsJson<CompareAllReport>()

        assertThat(res.totalNumberOfRequests, equalTo(2))
        assertThat(res.totalNumberOfCrns, equalTo(2))
        assertThat(res.currentPageNumber, equalTo(2))
        assertThat(res.unableToBeExecuted, equalTo(4))
    }

    @Test
    fun `compare when lao case`() {
        val res = mockMvc.perform(
            post("/secure/compareAll")
                .contentType("application/json;charset=utf-8")
                .content(
                    """
                    {
                        "pageNumber": 1,
                        "pageSize": 1,
                        "crns": [ "Y123456"],
                        "uriConfig": {
                            "OFFENDER_DETAIL": {}
                        }
                    }
                """
                )
                .withToken()
        ).andExpect(status().isOk).andReturn().response.contentAsJson<CompareAllReport>()

        assertThat(res.totalNumberOfRequests, equalTo(1))
    }
}
