package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.entity.contact.ContactRepository
import uk.gov.justice.digital.hmpps.entity.getAdjustmentReason
import uk.gov.justice.digital.hmpps.entity.staff.UserRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.CreateUnpaidWorkAdjustment
import uk.gov.justice.digital.hmpps.entity.unpaidwork.CreateUnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustment
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.Adjustment
import uk.gov.justice.digital.hmpps.model.AdjustmentPostResponse
import uk.gov.justice.digital.hmpps.model.AdjustmentReasonType
import uk.gov.justice.digital.hmpps.model.AdjustmentRequest
import uk.gov.justice.digital.hmpps.model.AdjustmentResponse
import uk.gov.justice.digital.hmpps.model.AdjustmentType
import java.time.ZonedDateTime

@Service
class AdjustmentService(
    private val adjustmentRepository: UnpaidWorkAdjustmentRepository,
    private val createUnpaidWorkAdjustmentRepository: CreateUnpaidWorkAdjustmentRepository,
    private val contactRepository: ContactRepository,
    private val unpaidWorkDetailsRepository: UpwDetailsRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    fun createAdjustments(
        adjustments: List<AdjustmentRequest>,
        crn: String,
        eventNumber: Int,
        username: String
    ): List<AdjustmentPostResponse> {
        val response = mutableListOf<AdjustmentPostResponse>()
        adjustments.forEach { adjustment ->
            val contact = contactRepository.findByExternalReferenceAndContactPersonCrnAndEventNumber(
                externalReference = adjustment.reference.toString(),
                crn = crn,
                number = eventNumber.toString()
            )
                ?: throw NotFoundException("Contact not found for reference ${adjustment.reference}, crn $crn and event number $eventNumber")
            val upwDetails = unpaidWorkDetailsRepository.findByEventIdIn(listOf(contact.event!!.id)).first()
            val user = userRepository.findByUsername(username)
                ?: throw NotFoundException("User not found for username $username")
            val adjustmentToSave = CreateUnpaidWorkAdjustment(
                id = 0L,
                detailsId = upwDetails.id,
                adjustmentAmount = adjustment.adjustmentAmountMinutes.toLong(),
                adjustmentDate = adjustment.date,
                adjustmentType = adjustment.adjustmentType.code,
                adjustmentReasonId = referenceDataRepository.getAdjustmentReason(adjustment.adjustmentReasonTypeCode).id,
                adjustedByUserId = user.id,
                softDeleted = false,
                createdDatetime = ZonedDateTime.now(),
                lastUpdatedDatetime = ZonedDateTime.now(),
                lastUpdatedUserId = user.id,
                createdByUserId = user.id,
                partitionAreaId = 0L,
            )
            val savedAdjustment = createUnpaidWorkAdjustmentRepository.save(adjustmentToSave)
            response.add(AdjustmentPostResponse(savedAdjustment.id!!, adjustment.reference.toString()))
        }
        return response
    }

    fun getAdjustments(crn: String, eventNumber: Int): AdjustmentResponse {
        val adjustments = mapExternalReferenceToAdjustment(crn, eventNumber)

        return AdjustmentResponse(
            adjustments =
                adjustments.map { (externalReference, it) ->
                    Adjustment(
                        id = it.id,
                        date = it.adjustmentDate,
                        reference = externalReference.substringAfterLast(":"),
                        adjustmentType = AdjustmentType.valueOf(it.adjustmentType),
                        adjustmentAmountMinutes = it.adjustmentAmount.toInt(),
                        adjustmentReasonType = AdjustmentReasonType(
                            code = it.adjustmentReason.code, name = it.adjustmentReason.description
                        ),

                        )
                })
    }

    fun mapExternalReferenceToAdjustment(crn: String, eventNumber: Int): Map<String, UnpaidWorkAdjustment> {
        val adjustments =
            adjustmentRepository.findExternalReferenceAndAdjustmentByCrnAndEventNumber(crn, eventNumber.toString())
        return adjustments.associate { row ->
            val externalReference = row[0] as String
            val adjustment = row[1] as UnpaidWorkAdjustment
            externalReference to adjustment
        }
    }

    fun updateAdjustment(
        adjustmentId: Long,
        adjustmentRequest: AdjustmentRequest,
        username: String
    ) {
        val existingAdjustment = createUnpaidWorkAdjustmentRepository.findFirstById(adjustmentId)
            ?: throw NotFoundException("Adjustment not found for id $adjustmentId")
        val userId = userRepository.findByUsername(username)?.id
            ?: throw NotFoundException("User not found for username $username")
        existingAdjustment.adjustmentType = adjustmentRequest.adjustmentType.code
        existingAdjustment.adjustmentAmount = adjustmentRequest.adjustmentAmountMinutes.toLong()
        existingAdjustment.adjustmentDate = adjustmentRequest.date
        existingAdjustment.lastUpdatedDatetime = ZonedDateTime.now()
        existingAdjustment.lastUpdatedUserId = userId
        existingAdjustment.adjustmentReasonId =
            referenceDataRepository.getAdjustmentReason(adjustmentRequest.adjustmentReasonTypeCode).id
        createUnpaidWorkAdjustmentRepository.save(existingAdjustment)
    }

    fun deleteAdjustment(adjustmentId: Long, username: String) {
        val existingAdjustment = createUnpaidWorkAdjustmentRepository.findFirstById(adjustmentId)
            ?: throw IllegalArgumentException("Adjustment not found for id $adjustmentId")
        val userId = userRepository.findByUsername(username)?.id
            ?: throw NotFoundException("User not found for username $username")
        existingAdjustment.softDeleted = true
        existingAdjustment.lastUpdatedDatetime = ZonedDateTime.now()
        existingAdjustment.lastUpdatedUserId = userId
        createUnpaidWorkAdjustmentRepository.save(existingAdjustment)
    }
}