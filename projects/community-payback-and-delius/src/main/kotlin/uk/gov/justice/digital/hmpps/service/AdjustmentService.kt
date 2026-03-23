package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.entity.getAdjustmentReason
import uk.gov.justice.digital.hmpps.entity.person.PersonRepository
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.staff.UserRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.CreateUnpaidWorkAdjustment
import uk.gov.justice.digital.hmpps.entity.unpaidwork.CreateUnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.Adjustment
import uk.gov.justice.digital.hmpps.model.AdjustmentPostResponse
import uk.gov.justice.digital.hmpps.model.AdjustmentReasonType
import uk.gov.justice.digital.hmpps.model.AdjustmentRequest
import uk.gov.justice.digital.hmpps.model.AdjustmentResponse
import uk.gov.justice.digital.hmpps.model.AdjustmentType

@Service
class AdjustmentService(
    private val adjustmentRepository: UnpaidWorkAdjustmentRepository,
    private val createUnpaidWorkAdjustmentRepository: CreateUnpaidWorkAdjustmentRepository,
    private val unpaidWorkDetailsRepository: UpwDetailsRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val userRepository: UserRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
) {
    fun createAdjustments(
        adjustments: List<AdjustmentRequest>,
        username: String
    ): List<AdjustmentPostResponse> {
        val user = userRepository.findByUsername(username)
            ?: throw NotFoundException("User not found for username $username")
        return adjustments.map { adjustment ->
            val person = personRepository.findByCrn(adjustment.crn).orNotFoundBy("CRN", adjustment.crn)
            val event = eventRepository.findByPersonIdAndNumberAndSoftDeletedIsFalse(
                person.id,
                adjustment.eventNumber.toString()
            )
                ?: throw NotFoundException("Event not found for CRN ${person.crn} and event number ${adjustment.eventNumber}")
            val upwDetails = unpaidWorkDetailsRepository.findByEventIdIn(listOf(event.id)).first()
            val adjustmentToSave = CreateUnpaidWorkAdjustment(
                detailsId = upwDetails.id,
                adjustmentAmount = adjustment.minutes,
                adjustmentDate = adjustment.date,
                adjustmentType = adjustment.type.code,
                adjustmentReasonId = referenceDataRepository.getAdjustmentReason(adjustment.reason).id,
                adjustedByUserId = user.id
            )
            val savedAdjustment = createUnpaidWorkAdjustmentRepository.save(adjustmentToSave)
            AdjustmentPostResponse(savedAdjustment.id!!)
        }
    }

    fun getAdjustments(crn: String, eventNumber: Int): AdjustmentResponse {
        val adjustments = adjustmentRepository.findByCrnAndEventNumber(crn, eventNumber.toString())

        return AdjustmentResponse(
            adjustments =
                adjustments.map { it ->
                    Adjustment(
                        id = it.id,
                        date = it.adjustmentDate,
                        type = AdjustmentType.valueOf(it.adjustmentType),
                        minutes = it.adjustmentAmount.toInt(),
                        reason = AdjustmentReasonType(
                            code = it.adjustmentReason.code, name = it.adjustmentReason.description
                        ),
                    )
                })
    }

    fun updateAdjustment(
        adjustmentId: Long,
        adjustmentRequest: AdjustmentRequest,
        username: String
    ) {
        val existingAdjustment =
            createUnpaidWorkAdjustmentRepository.findFirstById(adjustmentId).orNotFoundBy("Adjustment", adjustmentId)
        val userId = userRepository.findByUsername(username)?.id.orNotFoundBy("User", username)
        existingAdjustment.adjustmentType = adjustmentRequest.type.code
        existingAdjustment.adjustmentAmount = adjustmentRequest.minutes
        existingAdjustment.adjustmentDate = adjustmentRequest.date
        existingAdjustment.adjustedByUserId = userId
        existingAdjustment.adjustmentReasonId =
            referenceDataRepository.getAdjustmentReason(adjustmentRequest.reason).id
        createUnpaidWorkAdjustmentRepository.save(existingAdjustment)
    }

    fun deleteAdjustment(adjustmentId: Long, username: String) {
        val existingAdjustment = createUnpaidWorkAdjustmentRepository.findFirstById(adjustmentId)
            .orNotFoundBy("Adjustment", adjustmentId)
        val userId = userRepository.findByUsername(username)?.id
            .orNotFoundBy("User", username)
        createUnpaidWorkAdjustmentRepository.delete(existingAdjustment)
    }

    fun getAdjustment(id: Long): Adjustment {
        val adjustment = adjustmentRepository.findFirstById(id).orNotFoundBy("id", id)
        return Adjustment(
            id = adjustment.id,
            date = adjustment.adjustmentDate,
            type = AdjustmentType.valueOf(adjustment.adjustmentType),
            reason = AdjustmentReasonType(adjustment.adjustmentReason.code, adjustment.adjustmentReason.description),
            minutes = adjustment.adjustmentAmount.toInt()
        )
    }
}