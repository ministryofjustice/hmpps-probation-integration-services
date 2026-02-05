package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.model.RiskLevel
import uk.gov.justice.digital.hmpps.model.RoshDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoshDetailsTest @Autowired constructor(private val mockMvc: MockMvc) {

    @Test
    fun `should return rosh details`() {
        val roshDetails = mockMvc.get("/rosh/D006296") {
            withToken()
        }.andExpect {
            status { is2xxSuccessful() }
        }.andReturn().response.contentAsJson<RoshDetails>()

        assertThat(roshDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(roshDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))
        assertThat(roshDetails.rosh.riskChildrenCommunity)
            .isEqualTo(RiskLevel.HIGH)
        assertThat(roshDetails.rosh.riskPrisonersCustody)
            .isEqualTo(RiskLevel.HIGH)
        assertThat(roshDetails.rosh.riskStaffCustody)
            .isEqualTo(RiskLevel.LOW)
        assertThat(roshDetails.rosh.riskStaffCommunity)
            .isEqualTo(RiskLevel.HIGH)
        assertThat(roshDetails.rosh.riskKnownAdultCustody)
            .isEqualTo(RiskLevel.LOW)
        assertThat(roshDetails.rosh.riskKnownAdultCommunity)
            .isEqualTo(RiskLevel.MEDIUM)
        assertThat(roshDetails.rosh.riskPublicCustody)
            .isEqualTo(RiskLevel.LOW)
        assertThat(roshDetails.rosh.riskPublicCommunity)
            .isEqualTo(RiskLevel.LOW)
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc.get("/rosh/D000001") {
            withToken()
        }.andExpect {
            status { isNotFound() }
        }
    }
}
