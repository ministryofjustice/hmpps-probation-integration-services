package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.event.OrderManagerRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class InitialAllocationIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var orderManagerRepository: OrderManagerRepository

    @Test
    fun `returns csv report`() {
        orderManagerRepository.save(OrderManagerGenerator.UNALLOCATED)
        orderManagerRepository.save(OrderManagerGenerator.INITIAL_ALLOCATION)

        mockMvc
            .perform(get("/initial-allocations.csv").accept("text/csv").withToken())
            .andExpect(request().asyncStarted())
            .andDo(MvcResult::getAsyncResult)
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentTypeCompatibleWith("text/csv;charset=UTF-8"))
            .andExpect(
                content().string(
                    """
                    crn,eventNumber,sentenceType,allocatedBy,allocationDate,endDate,officerCode,teamCode,teamDescription,pduCode,pduDescription,providerCode,providerDescription,hasExclusion,hasRestriction
                    X123456,4,None,"HMPPS Allocations",07/05/2024,,"TEST01 ",N03AAA,"Description for N03AAA",PDU1,"Some PDU",N02,"NPS North East",N,N
                    
                    """.trimIndent()
                )
            )
    }
}
