create or replace package pkg_vpd_ctx as
    procedure set_client_identifier(p_username in varchar2);
    procedure clear_client_identifier;
end pkg_vpd_ctx;
grant execute on pkg_vpd_ctx to delius_app_schema;

create or replace package body pkg_vpd_ctx as
    procedure set_client_identifier(p_username in varchar2) is
    begin
    end set_client_identifier;

    procedure clear_client_identifier is
    begin
    end clear_client_identifier;
end pkg_vpd_ctx;
/
