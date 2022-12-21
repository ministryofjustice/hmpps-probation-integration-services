package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.DatasetCode
import java.time.ZonedDateTime

@JsonDeserialize(using = AllocationDetailDeserialiser::class)
sealed interface AllocationDetail {
    val id: String
    val staffCode: String
    val teamCode: String
    val providerCode: String
    val createdBy: String
    val createdDate: ZonedDateTime
    val datasetCode: DatasetCode
    val code: String

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class PersonAllocationDetail(
        override val id: String,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val crn: String,
        override val datasetCode: DatasetCode = DatasetCode.OM_ALLOCATION_REASON,
        override val code: String = "IN1"
    ) : AllocationDetail

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class EventAllocationDetail(
        override val id: String,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val eventNumber: Long,
        override val datasetCode: DatasetCode = DatasetCode.ORDER_ALLOCATION_REASON,
        override val code: String = "INT"
    ) : AllocationDetail

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class RequirementAllocationDetail(
        override val id: String,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val eventNumber: Long,
        val requirementId: Long,
        override val datasetCode: DatasetCode = DatasetCode.RM_ALLOCATION_REASON,
        override val code: String = "IN1"
    ) : AllocationDetail
}
