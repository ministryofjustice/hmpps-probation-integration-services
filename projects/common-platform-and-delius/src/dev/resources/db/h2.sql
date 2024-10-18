DROP SCHEMA IF EXISTS offender_support_api CASCADE;

CREATE SCHEMA offender_support_api;

CREATE ALIAS offender_support_api.getNextCRN AS
'String getNextCRN() {
    return "A111111";
}';