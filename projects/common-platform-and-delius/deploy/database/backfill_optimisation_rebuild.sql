-- Backfill: Rebuild optimisation tables for all remand cases created since 1 November 2026
-- This script runs the PROCREBUILDOPTTABLES procedure for each person with a remand case
-- created on or after 1 November 2026.

DECLARE
    CURSOR remand_cases_cursor IS
        SELECT DISTINCT o.offender_id
        FROM offender o
        JOIN event e ON e.offender_id = o.offender_id
        JOIN order_manager om ON om.event_id = e.event_id
        WHERE o.created_datetime >= TRUNC(TO_DATE('01-NOV-2026', 'DD-MON-YYYY'))
          AND o.soft_deleted = 0
          AND e.soft_deleted = 0
          AND om.soft_deleted = 0
        ORDER BY o.offender_id;
        
    v_offender_id offender.offender_id%TYPE;
    v_count INTEGER := 0;
    v_error_count INTEGER := 0;
BEGIN
    FOR remand_case IN remand_cases_cursor LOOP
        BEGIN
            v_offender_id := remand_case.offender_id;
            PKG_TRIGGERSUPPORT.PROCREBUILDOPTTABLES(v_offender_id);
            v_count := v_count + 1;
            
            -- Log progress every 100 records
            IF MOD(v_count, 100) = 0 THEN
                DBMS_OUTPUT.PUT_LINE('Processed ' || v_count || ' offenders');
            END IF;
        EXCEPTION
            WHEN OTHERS THEN
                v_error_count := v_error_count + 1;
                DBMS_OUTPUT.PUT_LINE('Error processing offender ' || v_offender_id || ': ' || SQLERRM);
        END;
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('');
    DBMS_OUTPUT.PUT_LINE('Backfill complete:');
    DBMS_OUTPUT.PUT_LINE('  Successfully processed: ' || v_count || ' offenders');
    DBMS_OUTPUT.PUT_LINE('  Errors encountered: ' || v_error_count);
END;
/
