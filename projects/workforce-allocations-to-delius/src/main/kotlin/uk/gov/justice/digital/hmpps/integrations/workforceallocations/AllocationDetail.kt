package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.DatasetCode
import java.time.ZonedDateTime

@JsonDeserialize(using = AllocationDetailDeserialiser::class)
sealed interface AllocationDetail {
    val id: String
    val staffCode: String
    val teamCode: String
    val createdDate: ZonedDateTime
    val datasetCode: DatasetCode
    val code: String

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class PersonAllocation(
        override val id: String,
        override val staffCode: String,
        override val teamCode: String,
        override val createdDate: ZonedDateTime,
        val crn: String,
        override val datasetCode: DatasetCode = DatasetCode.OM_ALLOCATION_REASON,
        override val code: String = "IN1"
    ) : AllocationDetail

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class EventAllocation(
        override val id: String,
        override val staffCode: String,
        override val teamCode: String,
        override val createdDate: ZonedDateTime,
        val eventNumber: Long,
        override val datasetCode: DatasetCode = DatasetCode.ORDER_ALLOCATION_REASON,
        override val code: String = "INT",
        @JsonAlias("allocationJustificationNotes")
        val notes: String?,
        val spoStaffCode: String?,
        @JsonAlias("sensitiveNotes")
        val sensitive: Boolean = true
    ) : AllocationDetail

    @JsonDeserialize(using = JsonDeserializer.None::class)
    data class RequirementAllocation(
        override val id: String,
        override val staffCode: String,
        override val teamCode: String,
        override val createdDate: ZonedDateTime,
        val eventNumber: Long,
        val requirementId: Long,
        override val datasetCode: DatasetCode = DatasetCode.RM_ALLOCATION_REASON,
        override val code: String = "IN1"
    ) : AllocationDetail
}
