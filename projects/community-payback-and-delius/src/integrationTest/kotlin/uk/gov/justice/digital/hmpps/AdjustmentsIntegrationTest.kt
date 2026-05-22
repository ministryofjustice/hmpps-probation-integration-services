package uk.gov.justice.digital.hmpps

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.web.servlet.*
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.service.AdjustmentService.Companion.REFERENCE_PREFIX
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.SECONDS
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
class AdjustmentsIntegrationTest @Autowired constructor(
    @Autowired private val mockMvc: MockMvc,
    private val adjustmentRepository: UnpaidWorkAdjustmentRepository,
    private val upwDetailsRepository: UpwDetailsRepository,
) {
    @Autowired
    lateinit var entityManager: EntityManager

    @Test
    fun `get unpaid work adjustment`() {
        val reference = UPWGenerator.ADJUSTMENT_NEGATIVE_FOR_ADJUSTMENT_PERSON.externalReference!!
            .removePrefix(REFERENCE_PREFIX)
        val response = mockMvc.get("/adjustments/$reference") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsJson<Adjustment>()
        assertThat(response.id).isEqualTo(UPWGenerator.ADJUSTMENT_NEGATIVE_FOR_ADJUSTMENT_PERSON.id)
        assertThat(response.reference.toString()).isEqualTo(reference)
        assertThat(response.date).isEqualTo(UPWGenerator.ADJUSTMENT_NEGATIVE_FOR_ADJUSTMENT_PERSON.date)
        assertThat(response.reason).isEqualTo(CodeName(name = "Other", code = "OT"))
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
        assertThat(adj.reference.toString()).isEqualTo(
            UPWGenerator.ADJUSTMENT_NEGATIVE_FOR_ADJUSTMENT_PERSON.externalReference!!
                .removePrefix(REFERENCE_PREFIX)
        )
        assertThat(adj.date).isEqualTo(UPWGenerator.ADJUSTMENT_NEGATIVE_FOR_ADJUSTMENT_PERSON.date)
        assertThat(adj.reason).isEqualTo(CodeName(name = "Other", code = "OT"))
        assertThat(adj.minutes).isEqualTo(3)
        assertThat(adj.id).isNotNull()
    }

    @Test
    fun `create upw adjustments`() {
        val crn = PersonGenerator.ADJUSTMENT_PERSON.crn
        val eventNumber = Integer.valueOf(UPWGenerator.EVENT_ADJUSTMENT.number)
        val username = UserGenerator.DEFAULT_USER.username
        val adjustmentType = AdjustmentType.POSITIVE
        val adjustmentReasonTypeCode = "OT"
        val adjustmentAmountMinutes = 10
        val body = listOf(
            CreateAdjustmentRequest(
                reference = UUID.randomUUID(),
                crn = crn,
                eventNumber = eventNumber,
                type = adjustmentType,
                date = LocalDate.now(),
                reason = adjustmentReasonTypeCode,
                minutes = adjustmentAmountMinutes
            ),
            CreateAdjustmentRequest(
                reference = UUID.randomUUID(),
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
        assertThat(response.map { it.id }).doesNotContainNull()
        assertThat(response.map { it.reference }).containsExactly(body[0].reference, body[1].reference)
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
            CreateAdjustmentRequest(
                reference = UUID.randomUUID(),
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
        val createdAdjustment = postResponse.first()
        mockMvc.delete("/adjustments/${createdAdjustment.reference}") { withToken() }
            .andExpect { status { isOk() } }
        val count = entityManager
            .createQuery("SELECT COUNT(a) FROM UnpaidWorkAdjustment a WHERE a.id = :id")
            .setParameter("id", createdAdjustment.id)
            .singleResult as Long
        assertThat(count).isEqualTo(0L)
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
            CreateAdjustmentRequest(
                reference = UUID.randomUUID(),
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
        val referenceToUpdate = postResponse.first().reference!!
        val updateBody = UpdateAdjustmentRequest(
            crn = crn,
            eventNumber = eventNumber,
            type = AdjustmentType.NEGATIVE,
            date = LocalDate.now(),
            reason = adjustmentReasonTypeCode,
            minutes = adjustmentAmountMinutes + 20
        )
        mockMvc.put("/adjustments/${referenceToUpdate}?username=${username}") {
            withToken()
            json = updateBody
        }.andExpect { status { isOk() } }
        val updatedAdjustment = adjustmentRepository.findByReference(referenceToUpdate)!!
        assertThat(updatedAdjustment.amount).isEqualTo(30)
        assertThat(updatedAdjustment.type).isEqualTo(AdjustmentType.NEGATIVE.name)
    }

    @Test
    @Transactional
    fun `creating an adjustment updates UPW status when hours are completed`() {
        val reference = UUID.randomUUID()
        val beingWorkedEvent = UPWGenerator.EVENT_5
        val beingWorkedDetails = UPWGenerator.UPW_DETAILS_5
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")

        mockMvc.post("/adjustments?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
            json = listOf(
                CreateAdjustmentRequest(
                    reference = reference,
                    crn = beingWorkedEvent.person.crn,
                    eventNumber = beingWorkedEvent.number.toInt(),
                    date = LocalDate.now(),
                    reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                    type = AdjustmentType.NEGATIVE,
                    minutes = 60,
                )
            )
        }.andExpect { status { isOk() } }

        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("HC")
    }

    @Test
    @Transactional
    fun `updating an adjustment updates UPW status when hours are completed`() {
        val reference = UUID.randomUUID()
        val beingWorkedEvent = UPWGenerator.EVENT_5
        val beingWorkedDetails = UPWGenerator.UPW_DETAILS_5
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")

        mockMvc.post("/adjustments?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
            json = listOf(
                CreateAdjustmentRequest(
                    reference = reference,
                    crn = beingWorkedEvent.person.crn,
                    eventNumber = beingWorkedEvent.number.toInt(),
                    date = LocalDate.now(),
                    reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                    type = AdjustmentType.POSITIVE,
                    minutes = 1,
                )
            )
        }.andExpect { status { isOk() } }
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")

        val statusUpdateStarted = ZonedDateTime.now()
        mockMvc.put("/adjustments/$reference?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
            json = UpdateAdjustmentRequest(
                crn = beingWorkedEvent.person.crn,
                eventNumber = beingWorkedEvent.number.toInt(),
                date = LocalDate.now(),
                reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                type = AdjustmentType.NEGATIVE,
                minutes = 60,
            )
        }.andExpect { status { isOk() } }
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("HC")
    }

    @Test
    @Transactional
    fun `deleting an adjustment updates UPW status when hours are completed`() {
        val positiveReference = UUID.randomUUID()
        val negativeReference = UUID.randomUUID()
        val beingWorkedEvent = UPWGenerator.EVENT_5
        val beingWorkedDetails = UPWGenerator.UPW_DETAILS_5
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")

        mockMvc.post("/adjustments?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
            json = listOf(
                CreateAdjustmentRequest(
                    reference = positiveReference,
                    crn = beingWorkedEvent.person.crn,
                    eventNumber = beingWorkedEvent.number.toInt(),
                    date = LocalDate.now(),
                    reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                    type = AdjustmentType.POSITIVE,
                    minutes = 100001,
                ),
                CreateAdjustmentRequest(
                    reference = negativeReference,
                    crn = beingWorkedEvent.person.crn,
                    eventNumber = beingWorkedEvent.number.toInt(),
                    date = LocalDate.now(),
                    reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                    type = AdjustmentType.NEGATIVE,
                    minutes = 60,
                )
            )
        }.andExpect { status { isOk() } }
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")

        mockMvc.delete("/adjustments/$positiveReference") { withToken() }.andExpect { status { isOk() } }
        val details = upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)
        assertThat(details?.status?.code).isEqualTo("HC")
        assertThat(details?.statusDate).isCloseTo(ZonedDateTime.now(), within(1, SECONDS))
    }

    @Test
    @Transactional
    fun `creating an adjustment that would result in negative remaining minutes returns bad request and does not persist changes`() {
        val reference = UUID.randomUUID()
        val beingWorkedEvent = UPWGenerator.EVENT_5
        val beingWorkedDetails = UPWGenerator.UPW_DETAILS_5

        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")

        mockMvc.post("/adjustments?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
            json = listOf(
                CreateAdjustmentRequest(
                    reference = reference,
                    crn = beingWorkedEvent.person.crn,
                    eventNumber = beingWorkedEvent.number.toInt(),
                    date = LocalDate.now(),
                    reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                    type = AdjustmentType.NEGATIVE,
                    minutes = 100000,
                )
            )
        }.andExpect { status { isBadRequest() } }.andExpect {
                content {
                    string(
                        org.hamcrest.Matchers.containsString(
                            "Adjustment would result in negative remaining unpaid work minutes"
                        )
                    )
                }
            }
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")
        val adjustment = adjustmentRepository.findByReference(reference)
        assertThat(adjustment).isNull()
    }

    @Test
    @Transactional
    fun `updating an adjustment that would result in negative remaining minutes returns bad request and does not persist changes`() {
        val reference = UUID.randomUUID()
        val beingWorkedEvent = UPWGenerator.EVENT_5
        val beingWorkedDetails = UPWGenerator.UPW_DETAILS_5
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")

        mockMvc.post("/adjustments?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
            json = listOf(
                CreateAdjustmentRequest(
                    reference = reference,
                    crn = beingWorkedEvent.person.crn,
                    eventNumber = beingWorkedEvent.number.toInt(),
                    date = LocalDate.now(),
                    reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                    type = AdjustmentType.POSITIVE,
                    minutes = 1,
                )
            )
        }.andExpect { status { isOk() } }

        val originalAdjustment = adjustmentRepository.findByReference(reference)!!
        assertThat(originalAdjustment.type).isEqualTo(AdjustmentType.POSITIVE.code)
        assertThat(originalAdjustment.amount).isEqualTo(1)

        mockMvc.put("/adjustments/$reference?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
            json = UpdateAdjustmentRequest(
                crn = beingWorkedEvent.person.crn,
                eventNumber = beingWorkedEvent.number.toInt(),
                date = LocalDate.now(),
                reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                type = AdjustmentType.NEGATIVE,
                minutes = 100000,
            )
        }.andExpect { status { isBadRequest() } }.andExpect {
                content {
                    string(
                        org.hamcrest.Matchers.containsString(
                            "Adjustment would result in negative remaining unpaid work minutes"
                        )
                    )
                }
            }
        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")
        val adjustmentAfterFailure = adjustmentRepository.findByReference(reference)!!
        assertThat(adjustmentAfterFailure.type).isEqualTo(AdjustmentType.POSITIVE.code)
        assertThat(adjustmentAfterFailure.amount).isEqualTo(1)
    }

    @Test
    @Transactional
    fun `deleting an adjustment that would result in negative remaining minutes returns bad request and does not persist changes`() {
        val positiveReference = UUID.randomUUID()
        val negativeReference = UUID.randomUUID()

        val beingWorkedEvent = UPWGenerator.EVENT_5
        val beingWorkedDetails = UPWGenerator.UPW_DETAILS_5

        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")

        mockMvc.post("/adjustments?username=${UserGenerator.DEFAULT_USER.username}") {
            withToken()
            json = listOf(
                CreateAdjustmentRequest(
                    reference = positiveReference,
                    crn = beingWorkedEvent.person.crn,
                    eventNumber = beingWorkedEvent.number.toInt(),
                    date = LocalDate.now(),
                    reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                    type = AdjustmentType.POSITIVE,
                    minutes = 100,
                ), CreateAdjustmentRequest(
                    reference = negativeReference,
                    crn = beingWorkedEvent.person.crn,
                    eventNumber = beingWorkedEvent.number.toInt(),
                    date = LocalDate.now(),
                    reason = ReferenceDataGenerator.UPW_ADJUSTMENT_REASON_OTHER.code,
                    type = AdjustmentType.NEGATIVE,
                    minutes = 90,
                )
            )
        }.andExpect { status { isOk() } }

        val positiveAdjustment = adjustmentRepository.findByReference(positiveReference)
        assertThat(positiveAdjustment).isNotNull()

        mockMvc.delete("/adjustments/$positiveReference") {
            withToken()
        }.andExpect { status { isBadRequest() } }.andExpect {
                content {
                    string(
                        org.hamcrest.Matchers.containsString(
                            "Adjustment would result in negative remaining unpaid work minutes"
                        )
                    )
                }
            }

        assertThat(upwDetailsRepository.findByIdOrNull(beingWorkedDetails.id)?.status?.code).isEqualTo("WK")
        val adjustmentAfterFailure = adjustmentRepository.findByReference(positiveReference)
        assertThat(adjustmentAfterFailure).isNotNull()
        assertThat(adjustmentAfterFailure!!.type).isEqualTo(AdjustmentType.POSITIVE.code)
        assertThat(adjustmentAfterFailure.amount).isEqualTo(100)
    }
}
