drop schema if exists pkg_vpd_ctx cascade;

create schema pkg_vpd_ctx;
create
alias pkg_vpd_ctx.set_client_identifier as 'void stub(String username) {}';
create
alias pkg_vpd_ctx.clear_client_identifier as 'void stub() {}';

create table if not exists object_type_document_template (
    document_template_id bigint not null,
    object_primary_key_id bigint not null
);