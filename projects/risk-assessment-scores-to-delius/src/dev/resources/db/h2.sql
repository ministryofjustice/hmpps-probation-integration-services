drop schema if exists pkg_triggersupport cascade;

create schema pkg_triggersupport;
create
alias pkg_triggersupport.procUpdateCAS as 'void stub(' ||
       'String p_crn, ' ||
       'Integer p_event_number, ' ||
       'java.util.Date p_rsr_assessor_date, ' ||
       'Double p_rsr_score, ' ||
       'String p_rsr_level_code, ' ||
       'Double p_osp_score_i, ' ||
       'Double p_osp_score_c, ' ||
       'String p_osp_level_i_code, ' ||
       'String p_osp_level_c_code, ' ||
       'String p_osp_level_iic_code, ' ||
       'String p_osp_level_dc_code) {}';
