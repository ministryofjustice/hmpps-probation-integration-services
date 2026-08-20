package uk.gov.justice.digital.hmpps.appointments

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_3
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointmentRepository
import uk.gov.justice.digital.hmpps.model.CreateAppointmentsRequest
import uk.gov.justice.digital.hmpps.model.CreatedAppointment
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import uk.gov.justice.digital.hmpps.test.TestData
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class DeleteAppointmentIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository,
) {
    @Test
    fun `delete with non-existent reference returns 404`() {
        val nonExistentReference = UUID.randomUUID()

        mockMvc
            .delete("/appointments/$nonExistentReference") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Appointment with reference $nonExistentReference not found")
            }
    }

    @Test
    fun `delete with invalid reference format returns 404`() {
        mockMvc
            .delete("/appointments/not-a-uuid") { withToken() }
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `delete without authentication returns 401`() {
        mockMvc
            .delete("/appointments/${UUID.randomUUID()}")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `delete a future appointment without an outcome succeeds`() {
        val reference = UUID.randomUUID()
        val request = TestData.createAppointment().copy(reference = reference)

        mockMvc
            .post("/projects/${UPW_PROJECT_3.code}/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(request))
            }
            .andExpect { status { isOk() } }

        mockMvc
            .delete("/appointments/$reference") { withToken() }
            .andExpect { status { isOk() } }
    }

    @Test
    fun `delete an appointment with an outcome returns 404`() {
        val reference = UUID.randomUUID()
        val request = TestData.createAppointmentWithOutcome().copy(
            reference = reference,
            date = LocalDate.now(),
        )

        val created = mockMvc
            .post("/projects/${UPW_PROJECT_3.code}/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(request))
            }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<CreatedAppointment>>().first()

        assertThat(unpaidWorkAppointmentRepository.findById(created.id)).isPresent

        mockMvc
            .delete("/appointments/$reference") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Appointment with reference $reference not found")
            }
    }

    @Test
    fun `delete a past appointment without an outcome returns 400`() {
        val reference = UUID.randomUUID()
        val request = TestData.createAppointment().copy(
            reference = reference,
            date = LocalDate.now().plusDays(1),
            startTime = LocalTime.of(0, 0),
            endTime = LocalTime.of(0, 1),
        )

        val created = mockMvc
            .post("/projects/${UPW_PROJECT_3.code}/appointments") {
                withToken()
                json = CreateAppointmentsRequest(listOf(request))
            }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<CreatedAppointment>>().first()

        // Move appointment to the past so it's no longer deletable
        val appointment = unpaidWorkAppointmentRepository.findById(created.id).get()
        appointment.date = LocalDate.now().minusDays(1)
        unpaidWorkAppointmentRepository.saveAndFlush(appointment)

        mockMvc
            .delete("/appointments/$reference") { withToken() }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Cannot delete past-dated appointment")
            }
    }
}
