package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import java.time.LocalDate
import java.util.stream.Stream

@JsonPropertyOrder(
    "crn",
    "eventNumber",
    "sentenceType",
    "allocatedBy",
    "allocationDate",
    "endDate",
    "officerCode",
    "teamCode",
    "teamDescription",
    "providerCode",
    "providerDescription"
)
interface InitialAllocation {
    val crn: String
    val eventNumber: Int
    val sentenceType: String
    val allocatedBy: String

    @get:JsonFormat(shape = STRING, pattern = "dd/MM/yyyy")
    val allocationDate: LocalDate

    @get:JsonFormat(shape = STRING, pattern = "dd/MM/yyyy")
    val endDate: LocalDate
    val officerCode: String
    val teamCode: String
    val teamDescription: String
    val pduCode: String
    val pduDescription: String
    val providerCode: String
    val providerDescription: String
}

@Repository
interface InitialAllocationRepository : JpaRepository<Person, Long> {
    @Query(
        value = """
            select 
                    allocation.crn,
                    allocation.event_number as "eventNumber",
                    allocation.sentence_type as "sentenceType",
                    (case when created_by.distinguished_name = 'HMPPSAllocations' then 'HMPPS Allocations' else 'Delius' end) as "allocatedBy",
                    allocation.allocation_date as "allocationDate",
                    allocation.end_date as "endDate",
                    staff.officer_code as "officerCode",
                    team.code as "teamCode",
                    team.description as "teamDescription",
                    borough.code as "pduCode",
                    borough.description as "pduDescription",
                    probation_area.code as "providerCode",
                    probation_area.description as "providerDescription"
            from (
                select 
                    offender.crn, 
                    event.event_number,
                    (case when r_disposal_type.sentence_type is null then 'None' when r_disposal_type.sentence_type in ('NC','SC') then 'Custody' else 'Community' end) as sentence_type,
                    order_manager.created_by_user_id,
                    order_manager.allocation_date,
                    order_manager.end_date,
                    order_manager.allocation_staff_id,
                    order_manager.allocation_team_id,
                    order_manager.probation_area_id,
                    lag(order_manager.allocation_staff_id) over (partition by order_manager.event_id order by allocation_date) as prev_staff_id
                from order_manager
                join event on event.event_id = order_manager.event_id and event.soft_deleted = 0
                left join disposal on disposal.event_id = event.event_id and disposal.soft_deleted = 0
                left join r_disposal_type on r_disposal_type.disposal_type_id = disposal.disposal_type_id
                join offender on offender.offender_id = event.offender_id and offender.soft_deleted = 0
                where order_manager.soft_deleted = 0
                and order_manager.allocation_date > :startDate
            ) allocation
            join user_ created_by on created_by.user_id = allocation.created_by_user_id
            join staff on staff.staff_id = allocation.allocation_staff_id and staff.officer_code not like '%U' and upper(staff.forename) || ' ' || upper(staff.surname) not like '%AWAITING ALLOCATION%'
            join staff previous_staff on previous_staff.staff_id = allocation.prev_staff_id and (previous_staff.officer_code like '%U' or upper(previous_staff.forename) || ' ' || upper(previous_staff.surname) like '%AWAITING ALLOCATION%')
            join team on team.team_id = allocation.allocation_team_id
            join district on district.district_id = team.district_id
            join borough on borough.borough_id = district.borough_id
            join probation_area on probation_area.probation_area_id = allocation.probation_area_id
            order by allocation.allocation_date desc
        """,
        nativeQuery = true
    )
    fun findAllInitialAllocations(startDate: LocalDate = LocalDate.ofYearDay(2024, 1)): Stream<InitialAllocation>
}
