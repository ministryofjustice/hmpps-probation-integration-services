package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class InitialAllocationIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returns csv report`() {
        mockMvc
            .perform(get("/initial-allocations.csv").accept("text/csv").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentTypeCompatibleWith("text/csv;charset=UTF-8"))
            .andExpect(
                content().string(
                    """
                    crn,eventNumber,sentenceType,allocatedBy,allocationDate,endDate,officerCode,teamCode,teamDescription,providerCode,providerDescription,pduCode,pduDescription
                    X123456,4,None,"HMPPS Allocations",01/07/2022,,"TEST01 ",N03AAA,"Description for N03AAA",N02,"NPS North East",PDU1,"Some PDU"
                    
                    """.trimIndent()
                )
            )
    }
}
