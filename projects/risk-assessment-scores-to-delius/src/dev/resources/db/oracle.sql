create or replace package pkg_triggersupport as
    procedure procUpdateCas(
        p_crn in varchar2,
        p_event_number in int,
        p_rsr_assessor_date in timestamp,
        p_rsr_score in float,
        p_rsr_band in varchar2,
        p_rsr_level_code in varchar2,
        p_osp_score_i in float,
        p_osp_level_i_code in varchar2,
        p_osp_score_c in float,
        p_osp_level_c_code in varchar2,
        p_osp_level_iic_code in varchar2 default null,
        p_osp_level_dc_code in varchar2 default null
    );
end pkg_triggersupport;
grant execute on pkg_triggersupport to delius_app_schema;

create or replace package body pkg_triggersupport as
    procedure procUpdateCAS(
        p_crn in varchar2,
        p_event_number in int,
        p_rsr_assessor_date in timestamp,
        p_rsr_score in float,
        p_rsr_level_code in varchar2,
        p_osp_score_i in float,
        p_osp_level_i_code in varchar2,
        p_osp_score_c in float,
        p_osp_level_c_code in varchar2,
        p_osp_level_iic_code in varchar2 default null,
        p_osp_level_dc_code in varchar2 default null
    ) is
    begin
        -- For testing:
        if length(p_crn) != 7 then raise_application_error(-20000, 'Invalid CRN'); end if;
        -- ...
    end procUpdateCAS;
end pkg_triggersupport;
/
