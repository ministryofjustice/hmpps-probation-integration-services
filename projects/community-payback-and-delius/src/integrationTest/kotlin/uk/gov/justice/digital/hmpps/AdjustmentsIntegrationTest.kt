package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.model.AdjustmentPostResponse
import uk.gov.justice.digital.hmpps.model.AdjustmentRequest
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
    fun `get unpaid work adjustments`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        val eventNumber = UPWGenerator.EVENT_1.number
        mockMvc.get("/${crn}/event/${eventNumber}/adjustments") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                    {"adjustments":
                      [
                        {
                            "id":${UPWGenerator.DEFAULT_UPW_DETAILS_ADJUSTMENT_NEGATIVE.id},
                            "reference":"${UPWGenerator.DEFAULT_CONTACT_EXTERNAL_REFERENCE}",
                            "adjustmentType":"NEGATIVE",
                            "date":"${UPWGenerator.DEFAULT_UPW_DETAILS_ADJUSTMENT_NEGATIVE.adjustmentDate}",
                            "adjustmentReasonType":
                              {
                                "code":"OT",
                                "name":"Other"
                              },
                            "adjustmentAmountMinutes":3
                        }
                      ]
                    }""".trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `create upw adjustments`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        val eventNumber = UPWGenerator.EVENT_1.number
        val username = UserGenerator.DEFAULT_USER.username
        val reference = UPWGenerator.CONTACT_NO_ENFORCEMENT_EXTERNAL_REFERENCE
        val adjustmentType = AdjustmentType.POSITIVE
        val adjustmentReasonTypeCode = "OT"
        val adjustmentAmountMinutes = 10
        val body = listOf(
            AdjustmentRequest(
                reference = reference,
                adjustmentType = adjustmentType,
                date = LocalDate.now(),
                adjustmentReasonTypeCode = adjustmentReasonTypeCode,
                adjustmentAmountMinutes = adjustmentAmountMinutes
            ),
            AdjustmentRequest(
                reference = reference,
                adjustmentType = AdjustmentType.NEGATIVE,
                date = LocalDate.now(),
                adjustmentReasonTypeCode = adjustmentReasonTypeCode,
                adjustmentAmountMinutes = adjustmentAmountMinutes
            )
        )

        val response = mockMvc.post("/${crn}/event/${eventNumber}/adjustments?username=${username}") {
            withToken()
            json = body
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<List<AdjustmentPostResponse>>()

        assertThat(response).hasSize(2)
        assertThat(response).allSatisfy {
            assertThat(it.id).isNotNull()
            assertThat(it.reference).isEqualTo(reference.toString())
        }
    }

    @Test
    fun `delete upw adjustment`() {
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        val eventNumber = UPWGenerator.EVENT_1.number
        val username = UserGenerator.DEFAULT_USER.username
        val reference = UPWGenerator.CONTACT_NO_ENFORCEMENT_EXTERNAL_REFERENCE
        val adjustmentType = AdjustmentType.POSITIVE
        val adjustmentReasonTypeCode = "OT"
        val adjustmentAmountMinutes = 10
        val body = listOf(
            AdjustmentRequest(
                reference = reference,
                adjustmentType = adjustmentType,
                date = LocalDate.now(),
                adjustmentReasonTypeCode = adjustmentReasonTypeCode,
                adjustmentAmountMinutes = adjustmentAmountMinutes
            )
        )
        val postResponse = mockMvc.post("/${crn}/event/${eventNumber}/adjustments?username=${username}") {
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
        val crn = PersonGenerator.DEFAULT_PERSON.crn
        val eventNumber = UPWGenerator.EVENT_1.number
        val username = UserGenerator.DEFAULT_USER.username
        val reference = UPWGenerator.CONTACT_NO_ENFORCEMENT_EXTERNAL_REFERENCE
        val adjustmentType = AdjustmentType.POSITIVE
        val adjustmentReasonTypeCode = "OT"
        val adjustmentAmountMinutes = 10
        val body = listOf(
            AdjustmentRequest(
                reference = reference,
                adjustmentType = adjustmentType,
                date = LocalDate.now(),
                adjustmentReasonTypeCode = adjustmentReasonTypeCode,
                adjustmentAmountMinutes = adjustmentAmountMinutes
            )
        )
        val postResponse = mockMvc.post("/${crn}/event/${eventNumber}/adjustments?username=${username}") {
            withToken()
            json = body
        }.andReturn().response.contentAsJson<List<AdjustmentPostResponse>>()
        val idToUpdate = postResponse.first().id
        val updateBody = AdjustmentRequest(
            reference = reference,
            adjustmentType = AdjustmentType.NEGATIVE,
            date = LocalDate.now(),
            adjustmentReasonTypeCode = adjustmentReasonTypeCode,
            adjustmentAmountMinutes = adjustmentAmountMinutes + 20
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
