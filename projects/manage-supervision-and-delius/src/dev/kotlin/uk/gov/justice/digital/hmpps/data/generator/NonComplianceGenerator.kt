package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * Self-contained generator for NonComplianceDetail integration test data.
 * Kept separate to avoid the circular static-init between PersonGenerator and ContactGenerator.
 */
object NonComplianceGenerator {

    // One contact of each non-compliant category, plus one that should be ignored (no outcome)
    val ACCEPTABLE_ABSENCE_CONTACT = ContactGenerator.generateContact(
        person = PersonGenerator.NON_COMPLIANCE_PERSON,
        contactType = ContactGenerator.APPT_CT_1,
        startDateTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).minusDays(3), EuropeLondon),
        outcome = ContactGenerator.ACCEPTABLE_ABSENCE,
        event = PersonGenerator.NON_COMPLIANCE_EVENT,
    )
    val UNACCEPTABLE_ABSENCE_CONTACT = ContactGenerator.generateContact(
        person = PersonGenerator.NON_COMPLIANCE_PERSON,
        contactType = ContactGenerator.APPT_CT_1,
        startDateTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).minusDays(2), EuropeLondon),
        outcome = ContactGenerator.FAILED_TO_COMPLY,
        event = PersonGenerator.NON_COMPLIANCE_EVENT,
    )
    val ATTENDED_NOT_COMPLY_CONTACT = ContactGenerator.generateContact(
        person = PersonGenerator.NON_COMPLIANCE_PERSON,
        contactType = ContactGenerator.APPT_CT_1,
        startDateTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).minusDays(1), EuropeLondon),
        outcome = ContactGenerator.ATTENDED_NOT_COMPLY_OUTCOME,
        event = PersonGenerator.NON_COMPLIANCE_EVENT,
    )
    val COMPLIANT_CONTACT = ContactGenerator.generateContact(
        person = PersonGenerator.NON_COMPLIANCE_PERSON,
        contactType = ContactGenerator.APPT_CT_1,
        startDateTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).minusDays(4), EuropeLondon),
        outcome = null,
        event = PersonGenerator.NON_COMPLIANCE_EVENT,
    )

    /**
     * An unacceptable absence created 14 months ago — well outside a 6-month window.
     * Used to verify the months filter excludes stale contacts.
     */
    val OLD_UNACCEPTABLE_ABSENCE_CONTACT = ContactGenerator.generateContact(
        person = PersonGenerator.NON_COMPLIANCE_PERSON,
        contactType = ContactGenerator.APPT_CT_1,
        startDateTime = ZonedDateTime.of(LocalDateTime.now(EuropeLondon).minusMonths(14), EuropeLondon),
        outcome = ContactGenerator.FAILED_TO_COMPLY,
        event = PersonGenerator.NON_COMPLIANCE_EVENT,
        createdDateTime = ZonedDateTime.now(EuropeLondon).minusMonths(14),
    )
}
