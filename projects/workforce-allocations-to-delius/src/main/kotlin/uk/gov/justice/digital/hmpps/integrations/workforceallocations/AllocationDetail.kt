package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
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

    @JsonDeserialize(using = JsonDeserializer.None::class)
    class PersonAllocationDetail(
        override val id: String,
        override val staffId: Long,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val crn: String,
        val personName: String,
        val staffGrade: String,
        val staffEmail: String,
        val staffForename: String,
        val staffSurname: String,
    ) : AllocationDetail

    @JsonDeserialize(using = JsonDeserializer.None::class)
    class EventAllocationDetail(
        override val id: String,
        override val staffId: Long,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val eventId: Long
    ) : AllocationDetail

    @JsonDeserialize(using = JsonDeserializer.None::class)
    class RequirementAllocationDetail(
        override val id: String,
        override val staffId: Long,
        override val staffCode: String,
        override val teamCode: String,
        override val providerCode: String,
        override val createdBy: String,
        override val createdDate: ZonedDateTime,
        val eventId: Long,
        val requirementId: Long,
    ) : AllocationDetail
}