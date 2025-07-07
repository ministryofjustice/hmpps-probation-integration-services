package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.UpwAppointment
import uk.gov.justice.digital.hmpps.model.UnpaidWorkAppointment
import java.time.LocalDate

interface UpwAppointmentRepository : JpaRepository<UpwAppointment, Long> {
    @Query(
        """
        select 
            any_value(first_name) as "firstName",
            any_value(formatted_mobile_number) as "mobileNumber", 
            any_value(to_char(appointment_date, 'DD/MM/YYYY')) as "appointmentDate",
            -- for testing --
            crn as "crn",
            listagg(distinct event_number, ', ') as "eventNumbers",
            listagg(distinct upw_appointment_id, ', ') as "upwAppointmentIds"
        from (
            with unique_mobile_numbers as (
                select 
                    replace(mobile_number, ' ', '') as formatted_mobile_number, 
                    any_value(offender_id) as offender_id
                from offender where soft_deleted = 0 
                -- no other cases with the same mobile number
                group by replace(mobile_number, ' ', '') having count(*) = 1)
            select
                first_name,
                formatted_mobile_number,
                upw_appointment.appointment_date as appointment_date,
                crn,
                event_number,
                upw_appointment.upw_appointment_id
            from unique_mobile_numbers
            join offender on offender.offender_id = unique_mobile_numbers.offender_id
            join event on event.offender_id = offender.offender_id and event.active_flag = 1 and event.soft_deleted = 0
            join disposal on disposal.event_id = event.event_id and disposal.active_flag = 1 and disposal.soft_deleted = 0
            join r_disposal_type on r_disposal_type.disposal_type_id = disposal.disposal_type_id
            join upw_details on upw_details.disposal_id = disposal.disposal_id and upw_details.soft_deleted = 0
            join upw_appointment on upw_appointment.upw_details_id = upw_details.upw_details_id and upw_appointment.soft_deleted = 0 and trunc(upw_appointment.appointment_date) = :date
            join upw_project on upw_project.upw_project_id = upw_appointment.upw_project_id and (:excludedProjectCodes is null or upw_project.code not in :excludedProjectCodes)
            join r_standard_reference_list upw_project_type on upw_project_type.standard_reference_list_id = upw_project.project_type_id and upw_project_type.code_value in ('ES','ICP','NP1','NP2','PIP','PIP2','PL','PSP','WH1')
            join probation_area on probation_area.probation_area_id = upw_project.probation_area_id and probation_area.code = :providerCode
            left join exclusion on exclusion.offender_id = offender.offender_id and exclusion_date < current_date and (exclusion_end_date is null or current_date < exclusion_end_date)
            left join restriction on restriction.offender_id = offender.offender_id and restriction_date < current_date and (restriction_end_date is null or current_date < restriction_end_date)
            -- valid mobile number
            where formatted_mobile_number like '07%' and length(formatted_mobile_number) = 11 and validate_conversion(formatted_mobile_number as number) = 1
            -- sms is allowed
            and (allow_sms is null or allow_sms = 'Y')
            -- no access limitations
            and restriction_id is null and exclusion_id is null
            -- appointment does not have an outcome
            and upw_appointment.contact_outcome_type_id is null
            -- not on remand
            and (current_remand_status is null or (current_remand_status not in ('Warrant With Bail', 'Warrant Without Bail', 'Remanded In Custody', 'Unlawfully at Large') and current_remand_status not like 'UAL%'))
            and not exists (select 1 from personal_circumstance 
                join r_circumstance_type on r_circumstance_type.circumstance_type_id = personal_circumstance.circumstance_type_id and r_circumstance_type.code_value = 'RIC'
                where personal_circumstance.offender_id = offender.offender_id and personal_circumstance.soft_deleted = 0 and (personal_circumstance.end_date is null or personal_circumstance.end_date > current_date))
            -- not in custody
            and not exists (select 1 from custody 
                join disposal custody_disposal on custody_disposal.disposal_id = custody.disposal_id and custody_disposal.active_flag = 1 and custody_disposal.soft_deleted = 0
                join event custody_event on custody_event.event_id = custody_disposal.event_id and custody_event.offender_id = offender.offender_id and custody_event.active_flag = 1 and custody_event.soft_deleted = 0
                join r_standard_reference_list custodial_status on custodial_status.standard_reference_list_id = custody.custodial_status_id and custodial_status.code_value in ('A', 'C', 'D')
                where custody.soft_deleted = 0)
            -- not on warrant or unlawfully at large
            and not exists (select 1 from registration 
                join r_register_type on r_register_type.register_type_id = registration.register_type_id and r_register_type.code in ('IWWO', 'IWWB', 'WRSM', 'HUAL')
                where registration.offender_id = offender.offender_id and registration.deregistered = 0 and registration.soft_deleted = 0)
            -- has an active unpaid work requirement
            and exists (select 1 from rqmnt 
                    join r_rqmnt_type_main_category on r_rqmnt_type_main_category.rqmnt_type_main_category_id = rqmnt.rqmnt_type_main_category_id and r_rqmnt_type_main_category.code in ('W', 'W0', 'W1', 'W2')
                    where rqmnt.disposal_id = disposal.disposal_id and rqmnt.active_flag = 1 and rqmnt.soft_deleted = 0)
            -- has unpaid work time remaining
            and (select total_minutes_ordered + positive_adjustments - negative_adjustments - minutes_credited from (
                    select 
                        case 
                            when r_disposal_type.pre_cja2003 = 'Y' then disposal.length * 60
                            else (select sum(rqmnt.length) * 60 from rqmnt 
                                    join r_rqmnt_type_main_category on r_rqmnt_type_main_category.rqmnt_type_main_category_id = rqmnt.rqmnt_type_main_category_id and r_rqmnt_type_main_category.code = 'W'
                                    where rqmnt.disposal_id = disposal.disposal_id and rqmnt.soft_deleted = 0)
                        end as total_minutes_ordered,
                        (select coalesce(sum(adjustment_amount), 0) from upw_adjustment where upw_adjustment.upw_details_id = upw_details.upw_details_id and adjustment_type = 'POSITIVE' and upw_adjustment.soft_deleted = 0)
                        as positive_adjustments,
                        (select coalesce(sum(adjustment_amount), 0) from upw_adjustment where upw_adjustment.upw_details_id = upw_details.upw_details_id and adjustment_type = 'NEGATIVE' and upw_adjustment.soft_deleted = 0)
                        as negative_adjustments,
                        (select coalesce(sum(appts.minutes_credited), 0) from upw_appointment appts where appts.upw_details_id = upw_details.upw_details_id and appts.soft_deleted = 0)
                        as minutes_credited
                    from dual)) > 0
        )
        group by crn
        order by crn
        """, nativeQuery = true
    )
    fun getUnpaidWorkAppointments(
        date: LocalDate,
        providerCode: String,
        excludedProjectCodes: List<String>
    ): List<UnpaidWorkAppointment>
}