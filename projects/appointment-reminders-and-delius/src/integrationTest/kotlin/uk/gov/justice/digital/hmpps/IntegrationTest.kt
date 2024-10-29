package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.model.UnpaidWorkAppointment
import uk.gov.justice.digital.hmpps.repository.UpwAppointmentRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var upwAppointmentRepository: UpwAppointmentRepository

    @Test
    fun `returns csv report`() {
        whenever(upwAppointmentRepository.getUnpaidWorkAppointments(any(), eq("N56"))).thenReturn(
            listOf(
                object : UnpaidWorkAppointment {
                    override val firstName = "Test"
                    override val mobileNumber = "07000000000"
                    override val appointmentDate = "01/01/2000"
                    override val appointmentTimes = "08:00, 11:00"
                    override val crn = "A123456"
                    override val eventNumber = "1"
                    override val upwAppointmentId = "123"
                }
            )
        )

        mockMvc
            .perform(get("/upw-appointments.csv?providerCode=N56").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentTypeCompatibleWith("text/csv;charset=UTF-8"))
            .andExpect(
                content().string(
                    """
                    firstName,mobileNumber,appointmentDate,appointmentTimes,crn,eventNumber,upwAppointmentId
                    Test,07000000000,01/01/2000,"08:00, 11:00",A123456,1,123
                    
                    """.trimIndent()
                )
            )
    }
}
