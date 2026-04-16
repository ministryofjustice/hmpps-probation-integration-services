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
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
) {
    fun createAdjustments(
        requests: List<CreateAdjustmentRequest>,
        username: String
    ): List<AdjustmentPostResponse> {
        val user = userRepository.findByUsername(username).orNotFoundBy("username", username)
        return requests.map { request ->
            val person = personRepository.findByCrn(request.crn).orNotFoundBy("CRN", request.crn)
            val event = eventRepository.findByPersonIdAndNumberAndSoftDeletedIsFalse(
                person.id,
                request.eventNumber.toString()
            )
                ?: throw NotFoundException("Event not found for CRN ${person.crn} and event number ${request.eventNumber}")
            val upwDetails = unpaidWorkDetailsRepository.findByEventIdIn(listOf(event.id)).first()
            val adjustment = adjustmentRepository.save(
                UnpaidWorkAdjustment(
                    upwDetails = upwDetails,
                    amount = request.minutes,
                    date = request.date,
                    type = request.type.code,
                    reason = referenceDataRepository.getAdjustmentReason(request.reason),
                    adjustedByUserId = user.id,
                    externalReference = "$REFERENCE_PREFIX${request.reference}"
                )
            )
            AdjustmentPostResponse(adjustment.id!!, adjustment.reference()!!)
        }
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

    @Deprecated("Pass a reference instead of an internal id")
    fun updateAdjustment(
        id: Long,
        adjustmentRequest: UpdateAdjustmentRequest,
        username: String
    ) {
        val existingAdjustment = adjustmentRepository.findFirstById(id).orNotFoundBy("id", id)
        val user = userRepository.findByUsername(username).orNotFoundBy("username", username)
        existingAdjustment.type = adjustmentRequest.type.code
        existingAdjustment.amount = adjustmentRequest.minutes
        existingAdjustment.date = adjustmentRequest.date
        existingAdjustment.adjustedByUserId = user.id
        existingAdjustment.reason = referenceDataRepository.getAdjustmentReason(adjustmentRequest.reason)
        adjustmentRepository.save(existingAdjustment)
    }

    fun updateAdjustment(
        reference: UUID,
        adjustmentRequest: UpdateAdjustmentRequest,
        username: String
    ) {
        val existingAdjustment = adjustmentRepository.findByReference(reference).orNotFoundBy("reference", reference)
        val user = userRepository.findByUsername(username).orNotFoundBy("username", username)
        existingAdjustment.type = adjustmentRequest.type.code
        existingAdjustment.amount = adjustmentRequest.minutes
        existingAdjustment.date = adjustmentRequest.date
        existingAdjustment.adjustedByUserId = user.id
        existingAdjustment.reason = referenceDataRepository.getAdjustmentReason(adjustmentRequest.reason)
        adjustmentRepository.save(existingAdjustment)
    }

    @Deprecated("Pass a reference instead of an internal id")
    fun deleteAdjustment(id: Long) {
        val existingAdjustment = adjustmentRepository.findFirstById(id).orNotFoundBy("id", id)
        adjustmentRepository.delete(existingAdjustment)
    }

    fun deleteAdjustment(reference: UUID) {
        val existingAdjustment = adjustmentRepository.findByReference(reference).orNotFoundBy("reference", reference)
        adjustmentRepository.delete(existingAdjustment)
    }

    @Deprecated("Pass a reference instead of an internal id")
    fun getAdjustment(id: Long): Adjustment {
        val adjustment = adjustmentRepository.findFirstById(id).orNotFoundBy("id", id)
        return Adjustment(
            id = adjustment.id!!,
            reference = adjustment.reference(),
            type = AdjustmentType.valueOf(adjustment.type),
            date = adjustment.date,
            reason = CodeName(name = adjustment.reason.description, code = adjustment.reason.code),
            minutes = adjustment.amount,
        )
    }

    fun getAdjustment(reference: UUID): Adjustment {
        val adjustment = adjustmentRepository.findByReference(reference).orNotFoundBy("reference", reference)
        return Adjustment(
            id = adjustment.id!!,
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
