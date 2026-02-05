package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.model.StaffDetail
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StaffControllerIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    fun `approved premises key worker staff are returned successfully`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT

        mockMvc
            .get("/approved-premises/${approvedPremises.code.code}/staff") { withToken() }
            .andExpect {
                status { isOk() }
                jsonPath("$.page.totalElements", equalTo(5))
                jsonPath("$.page.size", equalTo(100))
                jsonPath(
                    "$.content[*].name.surname",
                    equalTo(
                        listOf(
                            "Key-worker 1",
                            "Key-worker 2",
                            "Key-worker 3",
                            "Not key-worker",
                            "Unallocated"
                        )
                    )
                )
                jsonPath("$.content[*].keyWorker", equalTo(listOf(true, true, true, false, false)))
            }
    }

    @Test
    fun `empty approved premises returns 200 with empty results`() {
        val approvedPremises = ApprovedPremisesGenerator.NO_STAFF
        mockMvc.get("/approved-premises/${approvedPremises.code.code}/staff") {
            withToken()
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.size()") { value(equalTo(0)) }
            jsonPath("$.page.totalPages") { value(equalTo(0)) }
            jsonPath("$.page.totalElements") { value(equalTo(0)) }
        }
    }

    @Test
    fun `non-existent approved premises returns 404`() {
        mockMvc.get("/approved-premises/NOTFOUND/staff") {
            withToken()
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.message") { value(equalTo("Approved Premises with code of NOTFOUND not found")) }
        }
    }

    @Test
    fun `approved premises key workers only are returned successfully`() {
        val approvedPremises = ApprovedPremisesGenerator.DEFAULT
        mockMvc.get("/approved-premises/${approvedPremises.code.code}/staff?keyWorker=true") {
            withToken()
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.page.totalElements") { value(equalTo(3)) }
                jsonPath(
                    "$.content[*].name.surname"
                ) { value(equalTo(listOf("Key-worker 1", "Key-worker 2", "Key-worker 3"))) }
                jsonPath("$.content[*].keyWorker") { value(equalTo(listOf(true, true, true))) }
            }
    }

    @Test
    fun `Get staff by username`() {
        val username = StaffGenerator.DEFAULT_STAFF.user!!.username
        val res = mockMvc.get("/staff/$username") {
            withToken()
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsJson<StaffDetail>()

        assertThat(res.username, equalTo(username))
        assertThat(res.name.surname, equalTo(StaffGenerator.DEFAULT_STAFF.surname))
        assertThat(res.name.forename, equalTo(StaffGenerator.DEFAULT_STAFF.forename))
        assertThat(res.code, equalTo(StaffGenerator.DEFAULT_STAFF.code))
        assertThat(res.email, equalTo("john.smith@moj.gov.uk"))
        assertThat(res.telephoneNumber, equalTo("07321165373"))
        assertThat(res.teams[0].borough?.code, equalTo(StaffGenerator.DEFAULT_STAFF.teams[0].district.borough.code))
        assertThat(
            res.teams[0].borough?.description,
            equalTo(StaffGenerator.DEFAULT_STAFF.teams[0].district.borough.description)
        )
        assertThat(res.teams[0].startDate, equalTo(StaffGenerator.DEFAULT_STAFF.teams[0].startDate))
        assertThat(res.teams[0].endDate, equalTo(StaffGenerator.DEFAULT_STAFF.teams[0].endDate))
        assertThat(res.probationArea.code, equalTo(StaffGenerator.DEFAULT_STAFF.probationArea.code))
        assertThat(res.probationArea.description, equalTo(StaffGenerator.DEFAULT_STAFF.probationArea.description))
        assertThat(res.active, equalTo(true))
    }

    @Test
    fun `get staff by code`() {
        val staffCode = StaffGenerator.DEFAULT_STAFF.code
        val res = mockMvc.get("/staff?code=${staffCode}") {
            withToken()
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsJson<StaffDetail>()

        assertThat(res.username, equalTo(StaffGenerator.DEFAULT_STAFF.user!!.username))
        assertThat(res.name.surname, equalTo(StaffGenerator.DEFAULT_STAFF.surname))
        assertThat(res.name.forename, equalTo(StaffGenerator.DEFAULT_STAFF.forename))
        assertThat(res.code, equalTo(StaffGenerator.DEFAULT_STAFF.code))
    }

    @Test
    fun `get staff without username by code`() {
        val staffCode = StaffGenerator.STAFF_WITHOUT_USERNAME.code
        val res = mockMvc.get("/staff?code=${staffCode}") {
            withToken()
        }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsJson<StaffDetail>()

        assertThat(res.username, equalTo(null))
        assertThat(res.name.surname, equalTo(StaffGenerator.STAFF_WITHOUT_USERNAME.surname))
        assertThat(res.name.forename, equalTo(StaffGenerator.STAFF_WITHOUT_USERNAME.forename))
        assertThat(res.code, equalTo(StaffGenerator.STAFF_WITHOUT_USERNAME.code))
    }
}
