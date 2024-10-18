CREATE OR REPLACE PACKAGE offender_support_api IS
    FUNCTION getNextCRN RETURN VARCHAR2;
END offender_support_api;

GRANT EXECUTE ON offender_support_api TO delius_app_schema;

CREATE OR REPLACE PACKAGE BODY offender_support_api IS
    FUNCTION getNextCRN RETURN VARCHAR2 IS
    BEGIN
        RETURN 'A111111';
    END getNextCRN;
END offender_support_api;
/