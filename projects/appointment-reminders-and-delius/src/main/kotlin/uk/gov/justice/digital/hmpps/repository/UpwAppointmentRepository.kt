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
            crn,
            any_value(first_name) as first_name,
            any_value(mobile_number) as mobile_number, 
            any_value(to_char(appointment_date_time, 'DD/MM/YYYY')) as appointment_date,
            listagg(distinct to_char(appointment_date_time, 'HH24:MI'), ', ') as appointment_times,
            listagg(distinct next_work_session_project_type, ', ') as next_work_session_project_type,
            to_char(current_date, 'DD/MM/YYYY') as today,
            to_char(:date, 'DD/MM/YYYY') as send_sms_for_day,
            any_value(full_name) as full_name,
            count(distinct event_number) as number_of_events,
            listagg(distinct active_upw_requirements, ', ') as active_upw_requirements,
            listagg(distinct custodial_status, ', ') as custodial_status,
            any_value(current_remand_status) as current_remand_status,
            any_value(allow_sms) as allow_sms,
            any_value(original_mobile_number) as original_mobile_number,
            listagg(distinct upw_minutes_remaining, ', ') as upw_minutes_remaining
        from (
            with duplicate_mobile_numbers as (
                select offender.offender_id from offender 
                join offender duplicate on replace(duplicate.mobile_number, ' ', '') = replace(offender.mobile_number, ' ', '') and duplicate.offender_id <> offender.offender_id and duplicate.soft_deleted = 0
                where offender.soft_deleted = 0
            )
            select
                crn,
                upw_appointment.upw_appointment_id,
                event_number,
                first_name,
                replace(mobile_number, ' ', '') as mobile_number,
                upw_appointment.appointment_date + (upw_appointment.start_time - trunc(upw_appointment.start_time)) as appointment_date_time,
                upw_project_type.code_description as next_work_session_project_type,
                r_contact_outcome_type.description as outcome_description,
                to_char(current_date, 'DD/MM/YYYY') as today,
                to_char(:date, 'DD/MM/YYYY') as send_sms_for_day,
                first_name || ' ' || surname as full_name,
                1 as number_of_events,
                (select count(*) from rqmnt 
                    join r_rqmnt_type_main_category on r_rqmnt_type_main_category.rqmnt_type_main_category_id = rqmnt.rqmnt_type_main_category_id and r_rqmnt_type_main_category.code in ('W', 'W0', 'W1', 'W2')
                    where rqmnt.disposal_id = disposal.disposal_id and rqmnt.active_flag = 1 and rqmnt.soft_deleted = 0) as active_upw_requirements,
                custodial_status.code_description as custodial_status,
                case when exists (select 1 from registration 
                                join r_register_type on r_register_type.register_type_id = registration.register_type_id and r_register_type.code in ('IWWO', 'IWWB', 'WRSM')
                                where registration.offender_id = offender.offender_id and registration.deregistered = 0 and registration.soft_deleted = 0) then 'Y' else 'N' end as warrant,
                case when exists (select 1 from registration 
                                join r_register_type on r_register_type.register_type_id = registration.register_type_id and r_register_type.code in ('HUAL')
                                where registration.offender_id = offender.offender_id and registration.deregistered = 0 and registration.soft_deleted = 0) then 'Y' else 'N' end as unlawfully_at_large,
                current_remand_status,
                case when exists (select 1 from personal_circumstance 
                                join r_circumstance_type on r_circumstance_type.circumstance_type_id = personal_circumstance.circumstance_type_id and r_circumstance_type.code_value = 'RIC'
                                where personal_circumstance.offender_id = offender.offender_id and personal_circumstance.soft_deleted = 0
                ) then 'Y' else 'N' end as remanded_in_custody_circumstance,
                allow_sms,
                mobile_number as original_mobile_number,
                (select total_minutes_ordered + positive_adjustments - negative_adjustments - minutes_credited from (
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
                    from dual
                )) as upw_minutes_remaining
            from offender
            join event on event.offender_id = offender.offender_id and event.active_flag = 1 and event.soft_deleted = 0
            join disposal on disposal.event_id = event.event_id and disposal.active_flag = 1 and disposal.soft_deleted = 0
            join r_disposal_type on r_disposal_type.disposal_type_id = disposal.disposal_type_id
            join upw_details on upw_details.disposal_id = disposal.disposal_id and upw_details.soft_deleted = 0
            join upw_appointment on upw_appointment.upw_details_id = upw_details.upw_details_id and upw_appointment.soft_deleted = 0 and trunc(upw_appointment.appointment_date) = :date
            left join r_contact_outcome_type on r_contact_outcome_type.contact_outcome_type_id = upw_appointment.contact_outcome_type_id
            join upw_project on upw_project.upw_project_id = upw_appointment.upw_project_id
            join r_standard_reference_list upw_project_type on upw_project_type.standard_reference_list_id = upw_project.project_type_id and upw_project_type.code_value in :projectTypeCodes
            join probation_area on probation_area.probation_area_id = upw_project.probation_area_id and probation_area.code = :providerCode
            left join custody on custody.disposal_id = disposal.disposal_id and custody.soft_deleted = 0
            left join r_standard_reference_list custodial_status on custodial_status.standard_reference_list_id = custody.custodial_status_id
            where offender.soft_deleted = 0 
            -- allow_sms <> 'N'
            and (allow_sms is null or allow_sms = 'Y')
            -- mobile_number_is_valid = 'Y'
            and replace(mobile_number, ' ', '') like '07%'
                 and length(replace(mobile_number, ' ', '')) = 11
                 and validate_conversion(replace(mobile_number, ' ', '') as number) = 1
            -- duplicate_mobile_number_exists = 'N'
            and not exists (select 1 from duplicate_mobile_numbers where offender.offender_id = duplicate_mobile_numbers.offender_id)
            -- outcome_description = null
            and r_contact_outcome_type.description is null
        )
        where active_upw_requirements > 0 
        and upw_minutes_remaining > 0
        and warrant = 'N'
        and unlawfully_at_large = 'N'
        and current_remand_status is null or (current_remand_status not like 'Remand%' and current_remand_status not like 'Warrant%' and current_remand_status not like 'UAL%' and current_remand_status <> 'Unlawfully at Large')
        and remanded_in_custody_circumstance = 'N'
        group by crn
        """, nativeQuery = true
    )
    fun getUnpaidWorkAppointments(
        date: LocalDate,
        providerCode: String,
        projectTypeCodes: List<String>,
    ): List<UnpaidWorkAppointment>
}