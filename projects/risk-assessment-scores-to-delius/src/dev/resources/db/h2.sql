drop schema if exists pkg_triggersupport cascade;

create schema pkg_triggersupport;
create alias pkg_triggersupport.procUpdateCAS as 'void stub() {}';
