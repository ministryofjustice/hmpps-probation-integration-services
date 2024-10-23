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
        whenever(upwAppointmentRepository.getUnpaidWorkAppointments(any(), eq("N56"), any())).thenReturn(
            listOf(
                UnpaidWorkAppointment(
                    crn = "A123456",
                    firstName = "Test",
                    mobileNumber = "07000000000",
                    appointmentDate = "01/01/2000",
                    appointmentTimes = "08:00, 11:00",
                    nextWorkSessionProjectType = "Group session",
                    today = "01/01/2000",
                    sendSmsForDay = "01/01/2000",
                    fullName = "Test Test",
                    numberOfEvents = "1",
                    activeUpwRequirements = "1",
                    custodialStatus = null,
                    currentRemandStatus = null,
                    allowSms = "Y",
                    originalMobileNumber = "070 0000 0000",
                    upwMinutesRemaining = "123"
                )
            )
        )

        mockMvc
            .perform(get("/upw-appointments.csv?providerCode=N56").withToken())
            .andExpect(status().is2xxSuccessful)
            .andExpect(content().contentTypeCompatibleWith("text/csv;charset=UTF-8"))
            .andExpect(
                content().string(
                    """
                    crn,firstName,mobileNumber,appointmentDate,appointmentTimes,"nextWorkSessionProjectType",today,sendSmsForDay,fullName,numberOfEvents,activeUpwRequirements,custodialStatus,currentRemandStatus,allowSms,originalMobileNumber,upwMinutesRemaining
                    A123456,Test,07000000000,01/01/2000,"08:00, 11:00","Group session",01/01/2000,01/01/2000,"Test Test",1,1,,,Y,"070 0000 0000",123
                    
                    """.trimIndent()
                )
            )
    }
}
