package uk.gov.justice.digital.hmpps.integrations.delius.document

enum class DocumentType(val description: String) {
    OFFENDER_DOCUMENT("Offender related"),
    CONVICTION_DOCUMENT("Sentence related"),
    CPSPACK_DOCUMENT("Crown Prosecution Service case pack"),
    PRECONS_DOCUMENT("PNC previous convictions"),
    COURT_REPORT_DOCUMENT("Court report"),
    INSTITUTION_REPORT_DOCUMENT("Institution report"),
    ADDRESS_ASSESSMENT_DOCUMENT("Address assessment related document"),
    APPROVED_PREMISES_REFERRAL_DOCUMENT("Approved premises referral related document"),
    ASSESSMENT_DOCUMENT("Assessment document"),
    CASE_ALLOCATION_DOCUMENT("Case allocation document"),
    PERSONAL_CONTACT_DOCUMENT("Personal contact related document"),
    REFERRAL_DOCUMENT("Referral related document"),
    NSI_DOCUMENT("Non Statutory Intervention related document"),
    PERSONAL_CIRCUMSTANCE_DOCUMENT("Personal circumstance related document"),
    UPW_APPOINTMENT_DOCUMENT("Unpaid work appointment document"),
    CONTACT_DOCUMENT("Contact related document");

    companion object {
        fun of(tableName: String, type: String): DocumentType = when (tableName) {
            "OFFENDER" -> if (type == "PREVIOUS_CONVICTION") PRECONS_DOCUMENT else OFFENDER_DOCUMENT
            "EVENT" -> if (type == "CPS_PACK") CPSPACK_DOCUMENT else CONVICTION_DOCUMENT
            "COURT_REPORT" -> COURT_REPORT_DOCUMENT
            "INSTITUTIONAL_REPORT" -> INSTITUTION_REPORT_DOCUMENT
            "ADDRESSASSESSMENT" -> ADDRESS_ASSESSMENT_DOCUMENT
            "APPROVED_PREMISES_REFERRAL" -> APPROVED_PREMISES_REFERRAL_DOCUMENT
            "ASSESSMENT" -> ASSESSMENT_DOCUMENT
            "CASE_ALLOCATION" -> CASE_ALLOCATION_DOCUMENT
            "PERSONALCONTACT" -> PERSONAL_CONTACT_DOCUMENT
            "REFERRAL" -> REFERRAL_DOCUMENT
            "NSI" -> NSI_DOCUMENT
            "PERSONAL_CIRCUMSTANCE" -> PERSONAL_CIRCUMSTANCE_DOCUMENT
            "UPW_APPOINTMENT" -> UPW_APPOINTMENT_DOCUMENT
            "CONTACT" -> CONTACT_DOCUMENT
            else -> error("Un-mapped document type ($tableName/$type)")
        }
    }
}
