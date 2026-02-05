package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.model.NeedsDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NeedsDetailTest @Autowired constructor(private val mockMvc: MockMvc) {

    @Test
    fun `should return needs details`() {
        val needsDetails = mockMvc.get("/needs-details/D006296") {
            withToken()
        }.andExpect {
            status { is2xxSuccessful() }
        }.andReturn().response.contentAsJson<NeedsDetails>()

        assertThat(needsDetails.initiationDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-09T14:33:53Z").withZoneSameInstant(EuropeLondon))
        assertThat(needsDetails.lastUpdatedDate)
            .isEqualTo(ZonedDateTime.parse("2022-11-17T15:02:17Z").withZoneSameInstant(EuropeLondon))
        assertThat(needsDetails.needs?.financeIssuesDetails)
            .isEqualTo("no money")
        assertThat(needsDetails.needs?.attitudeIssuesDetails)
            .isEqualTo("likes to behave badly")
        assertThat(needsDetails.dateCompleted).isNull()
        assertThat(needsDetails.linksToHarm?.alcoholLinkedToHarm).isFalse
        assertThat(needsDetails.linksToHarm?.drugLinkedToHarm).isFalse
        assertThat(needsDetails.linksToHarm?.educationTrainingEmploymentLinkedToHarm).isTrue
    }

    @Test
    fun `should return HTTP not found when CRN does not exist`() {
        mockMvc.get("/needs-details/D000001") {
            withToken()
        }.andExpect {
            status { isNotFound() }
        }
    }
}
