package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class PersonDetail(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate,
    val contactDetails: ContactDetails?
)

data class ContactDetails(val telephone: String?, val mobile: String?, val email: String?) {
    companion object {
        fun of(telephone: String?, mobile: String?, email: String?): ContactDetails? =
            if (telephone == null && mobile == null && email == null) {
                null
            } else {
                ContactDetails(telephone, mobile, email)
            }
    }
}