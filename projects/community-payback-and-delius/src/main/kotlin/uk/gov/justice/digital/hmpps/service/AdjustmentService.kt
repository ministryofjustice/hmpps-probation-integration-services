package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.entity.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.entity.getAdjustmentReason
import uk.gov.justice.digital.hmpps.entity.person.PersonRepository
import uk.gov.justice.digital.hmpps.entity.sentence.EventRepository
import uk.gov.justice.digital.hmpps.entity.staff.UserRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustment
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAdjustmentRepository
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UpwDetailsRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy
import uk.gov.justice.digital.hmpps.model.*
import java.util.*

@Service
class AdjustmentService(
    private val adjustmentRepository: UnpaidWorkAdjustmentRepository,
    private val unpaidWorkDetailsRepository: UpwDetailsRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val userRepository: UserRepository,
    private val personRepository: PersonRepository,
    private val eventRepository: EventRepository,
    private val communityPaybackAppointmentsService: CommunityPaybackAppointmentsService,
) {
    fun createAdjustments(
        requests: List<CreateAdjustmentRequest>,
        username: String
    ): List<AdjustmentPostResponse> {
        val user = userRepository.findByUsername(username).orNotFoundBy("username", username)
        val events = eventRepository.getEventIds(requests.map { it.crn to it.eventNumber })
        val upwDetails = unpaidWorkDetailsRepository.getByEventIdIn(events.values)
        return requests.map { request ->
            val eventId = checkNotNull(events[request.crn to request.eventNumber])
            val details = checkNotNull(upwDetails[eventId])
            val projectedRemainingMinutes = communityPaybackAppointmentsService.projectedRemainingMinutes(
                details = details,
                newAmount = request.minutes.toLong(),
                newType = request.type,
            )
            communityPaybackAppointmentsService.validateRemainingMinutes(projectedRemainingMinutes)

            val adjustment = adjustmentRepository.save(
                UnpaidWorkAdjustment(
                    upwDetails = details,
                    amount = request.minutes,
                    date = request.date,
                    type = request.type.code,
                    reason = referenceDataRepository.getAdjustmentReason(request.reason),
                    adjustedByUserId = user.id,
                    externalReference = "$REFERENCE_PREFIX${request.reference}"
                )
            )
            AdjustmentPostResponse(adjustment.id!!, adjustment.reference()!!)
        }.also { upwDetails.values.onEach { communityPaybackAppointmentsService.updateStatus(it) } }
    }

    fun getAdjustments(crn: String, eventNumber: Int): AdjustmentResponse {
        val adjustments = adjustmentRepository.findByCrnAndEventNumber(crn, eventNumber.toString())

        return AdjustmentResponse(adjustments.map {
            Adjustment(
                id = it.id!!,
                reference = it.reference(),
                date = it.date,
                type = AdjustmentType.valueOf(it.type),
                minutes = it.amount,
                reason = CodeName(name = it.reason.description, code = it.reason.code),
            )
        })
    }

    fun updateAdjustment(
        reference: UUID,
        adjustmentRequest: UpdateAdjustmentRequest,
        username: String
    ) {
        val existingAdjustment = adjustmentRepository.findByReference(reference).orNotFoundBy("reference", reference)
        val user = userRepository.findByUsername(username).orNotFoundBy("username", username)
        val projectedRemainingMinutes = communityPaybackAppointmentsService.projectedRemainingMinutes(
            details = existingAdjustment.upwDetails,
            newAmount = adjustmentRequest.minutes.toLong(),
            newType = adjustmentRequest.type,
            existingAmount = existingAdjustment.amount.toLong(),
            existingType = AdjustmentType.valueOf(existingAdjustment.type),
        )
        communityPaybackAppointmentsService.validateRemainingMinutes(projectedRemainingMinutes)
        existingAdjustment.type = adjustmentRequest.type.code
        existingAdjustment.amount = adjustmentRequest.minutes
        existingAdjustment.date = adjustmentRequest.date
        existingAdjustment.adjustedByUserId = user.id
        existingAdjustment.reason = referenceDataRepository.getAdjustmentReason(adjustmentRequest.reason)
        adjustmentRepository.save(existingAdjustment)
        communityPaybackAppointmentsService.updateStatus(existingAdjustment.upwDetails)
    }

    fun deleteAdjustment(reference: UUID) {
        val existingAdjustment = adjustmentRepository.findByReference(reference).orNotFoundBy("reference", reference)
        val projectedRemainingMinutes = communityPaybackAppointmentsService.projectedRemainingMinutes(
            details = existingAdjustment.upwDetails,
            existingAmount = existingAdjustment.amount.toLong(),
            existingType = AdjustmentType.valueOf(existingAdjustment.type),
        )
        communityPaybackAppointmentsService.validateRemainingMinutes(projectedRemainingMinutes)
        adjustmentRepository.delete(existingAdjustment)
        communityPaybackAppointmentsService.updateStatus(existingAdjustment.upwDetails)
    }

    fun getAdjustment(reference: UUID): Adjustment {
        val adjustment = adjustmentRepository.findByReference(reference).orNotFoundBy("reference", reference)
        return Adjustment(
            id = checkNotNull(adjustment.id),
            reference = adjustment.reference(),
            type = AdjustmentType.valueOf(adjustment.type),
            date = adjustment.date,
            reason = CodeName(name = adjustment.reason.description, code = adjustment.reason.code),
            minutes = adjustment.amount,
        )
    }

    fun UnpaidWorkAdjustment.reference() =
        externalReference?.removePrefix(REFERENCE_PREFIX)?.let { UUID.fromString(it) }

    companion object {
        const val REFERENCE_PREFIX = "urn:uk:gov:hmpps:community-payback:adjustment:"
    }
}
