package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.model.Adjustment
import uk.gov.justice.digital.hmpps.model.AdjustmentPostResponse
import uk.gov.justice.digital.hmpps.model.AdjustmentReasonType
import uk.gov.justice.digital.hmpps.model.AdjustmentRequest
import uk.gov.justice.digital.hmpps.model.AdjustmentResponse
import uk.gov.justice.digital.hmpps.model.AdjustmentType
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
class AdjustmentsIntegrationTest @Autowired constructor(
    @Autowired private val mockMvc: MockMvc,
    private val adjustmentRepository: UnpaidWorkAdjustmentRepository
) {

    @Test
    fun `get unpaid work adjustment`() {
        val id = UPWGenerator.ADJUSTMENT_NEGATIVE_FOR_ADJUSTMENT_PERSON.id
        val response = mockMvc.get("/adjustments/$id") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<Adjustment>()
        assertThat(response.id).isEqualTo(id)
        assertThat(response.date).isEqualTo(UPWGenerator.ADJUSTMENT_NEGATIVE_FOR_ADJUSTMENT_PERSON.adjustmentDate)
        assertThat(response.reason).isEqualTo(AdjustmentReasonType(code = "OT", name = "Other"))
        assertThat(response.minutes).isEqualTo(3)
    }

    @Test
    fun `get unpaid work adjustments`() {
        val crn = PersonGenerator.ADJUSTMENT_PERSON.crn
        val eventNumber = UPWGenerator.EVENT_ADJUSTMENT.number
        val response = mockMvc.get("/adjustments?crn=${crn}&eventNumber=${eventNumber}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<AdjustmentResponse>()

        val adjustments = response.adjustments
        assertThat(adjustments).isNotNull
        assertThat(adjustments).hasSize(1)
        val adj = adjustments.first()
        assertThat(adj.type).isEqualTo(AdjustmentType.NEGATIVE)
        assertThat(adj.date).isEqualTo(UPWGenerator.GET_ADJUSTMENT_NEGATIVE.adjustmentDate)
        assertThat(adj.reason).isEqualTo(AdjustmentReasonType(code = "OT", name = "Other"))
        assertThat(adj.minutes).isEqualTo(3)
        assertThat(adj.id).isNotNull()
    }

    @Test
    fun `create upw adjustments`() {
        val crn = PersonGenerator.ADJUSTMENT_PERSON.crn
        val eventNumber = Integer.valueOf( UPWGenerator.EVENT_ADJUSTMENT.number)
        val username = UserGenerator.DEFAULT_USER.username
        val adjustmentType = AdjustmentType.POSITIVE
        val adjustmentReasonTypeCode = "OT"
        val adjustmentAmountMinutes = 10
        val body = listOf(
            AdjustmentRequest(
                crn = crn,
                eventNumber = eventNumber,
                type = adjustmentType,
                date = LocalDate.now(),
                reason = adjustmentReasonTypeCode,
                minutes = adjustmentAmountMinutes
            ),
            AdjustmentRequest(
                crn = crn,
                eventNumber = eventNumber,
                type = AdjustmentType.NEGATIVE,
                date = LocalDate.now(),
                reason = adjustmentReasonTypeCode,
                minutes = adjustmentAmountMinutes
            )
        )

        val response = mockMvc.post("/adjustments?username=${username}") {
            withToken()
            json = body
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<AdjustmentPostResponse>>()

        assertThat(response).hasSize(2)
        assertThat(response).allSatisfy {
            assertThat(it.id).isNotNull()
        }
    }

    @Test
    fun `delete upw adjustment`() {
        val crn = PersonGenerator.ADJUSTMENT_PERSON.crn
        val eventNumber = Integer.valueOf(UPWGenerator.EVENT_ADJUSTMENT.number)
        val username = UserGenerator.DEFAULT_USER.username
        val adjustmentType = AdjustmentType.POSITIVE
        val adjustmentReasonTypeCode = "OT"
        val adjustmentAmountMinutes = 10
        val body = listOf(
            AdjustmentRequest(
                crn = crn,
                eventNumber = eventNumber,
                type = adjustmentType,
                date = LocalDate.now(),
                reason = adjustmentReasonTypeCode,
                minutes = adjustmentAmountMinutes
            )
        )
        val postResponse = mockMvc.post("/adjustments?username=${username}") {
            withToken()
            json = body
        }.andExpect { status { isOk() } }.andReturn().response.contentAsJson<List<AdjustmentPostResponse>>()
        val idToDelete = postResponse.first().id
        mockMvc.delete("/adjustments/${idToDelete}?username=${username}") { withToken() }
            .andExpect { status { isOk() } }
        val deletedAdjustment = adjustmentRepository.findById(idToDelete)
        assertThat(deletedAdjustment).isEmpty()
    }

    @Test
    fun `update upw adjustment`() {
        val crn = PersonGenerator.ADJUSTMENT_PERSON.crn
        val eventNumber = Integer.valueOf(UPWGenerator.EVENT_ADJUSTMENT.number)
        val username = UserGenerator.DEFAULT_USER.username
        val adjustmentType = AdjustmentType.POSITIVE
        val adjustmentReasonTypeCode = "OT"
        val adjustmentAmountMinutes = 10
        val body = listOf(
            AdjustmentRequest(
                crn = crn,
                eventNumber = eventNumber,
                type = adjustmentType,
                date = LocalDate.now(),
                reason = adjustmentReasonTypeCode,
                minutes = adjustmentAmountMinutes
            )
        )
        val postResponse = mockMvc.post("/adjustments?username=${username}") {
            withToken()
            json = body
        }.andReturn().response.contentAsJson<List<AdjustmentPostResponse>>()
        val idToUpdate = postResponse.first().id
        val updateBody = AdjustmentRequest(
            crn = crn,
            eventNumber = eventNumber,
            type = AdjustmentType.NEGATIVE,
            date = LocalDate.now(),
            reason = adjustmentReasonTypeCode,
            minutes = adjustmentAmountMinutes + 20
        )
        mockMvc.put("/adjustments/${idToUpdate}?username=${username}") {
            withToken()
            json = updateBody
        }.andExpect { status { isOk() } }
        val updatedAdjustment = adjustmentRepository.findFirstById(idToUpdate)!!
        assertThat(updatedAdjustment.adjustmentAmount).isEqualTo(30)
        assertThat(updatedAdjustment.adjustmentType).isEqualTo(AdjustmentType.NEGATIVE.name)
    }
}
