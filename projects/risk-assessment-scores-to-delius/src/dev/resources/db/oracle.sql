-- As of 30/09/2022, the procedure is not yet available in the Delius OracleDB so I've left this SQL here to help with local testing.

create or replace package pkg_triggersupport as
    procedure procRsrUpdateCas(
        p_crn in varchar2,
        p_event_number in int,
        p_rsr_assessor_date in timestamp,
        p_rsr_score in float,
        p_rsr_band in varchar2,
        p_osp_indecent_score in float,
        p_osp_indecent_band in varchar2,
        p_osp_contact_score in float,
        p_osp_contact_band in varchar2,
        p_error out varchar2
    );
end pkg_triggersupport;
grant execute on pkg_triggersupport to delius_app_schema;

create or replace package body pkg_triggersupport as
    procedure procRsrUpdateCas(
        p_crn in varchar2,
        p_event_number in int,
        p_rsr_assessor_date in timestamp,
        p_rsr_score in float,
        p_rsr_band in varchar2,
        p_osp_indecent_score in float,
        p_osp_indecent_band in varchar2,
        p_osp_contact_score in float,
        p_osp_contact_band in varchar2,
        p_error out varchar2
    ) is
    begin
        -- For testing:
        if length(p_crn) != 7 then p_error := 'Invalid CRN'; end if;
        -- ...
    end procRsrUpdateCas;
end pkg_triggersupport;
/
