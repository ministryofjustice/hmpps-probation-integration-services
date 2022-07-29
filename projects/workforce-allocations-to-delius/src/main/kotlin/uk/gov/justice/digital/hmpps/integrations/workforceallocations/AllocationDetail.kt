package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationReasonMasterCode
import java.time.ZonedDateTime

@JsonDeserialize(using = AllocationDetailDeserialiser::class)
sealed interface AllocationDetail {
    val id: String
    val staffId: Long
    val staffCode: String
    val teamCode: String
    val providerCode: String
    val createdBy: String
    val createdDate: ZonedDateTime
    val masterCode: AllocationReasonMasterCode
    val code: String

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class PersonAllocationDetail(
        override val id: String,
        override val staffId: Long,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val crn: String,
        override val masterCode: AllocationReasonMasterCode = AllocationReasonMasterCode.OM_ALLOCATION_REASON,
        override val code: String = "INT1"
    ) : AllocationDetail

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class EventAllocationDetail(
        override val id: String,
        override val staffId: Long,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val eventId: Long,
        override val masterCode: AllocationReasonMasterCode = AllocationReasonMasterCode.ORDER_ALLOCATION_REASON,
        override val code: String = "INT"
    ) : AllocationDetail

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class RequirementAllocationDetail(
        override val id: String,
        override val staffId: Long,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val eventId: Long,
        val requirementId: Long,
        override val masterCode: AllocationReasonMasterCode = AllocationReasonMasterCode.RM_ALLOCATION_REASON,
        override val code: String = "INT1"
    ) : AllocationDetail
}