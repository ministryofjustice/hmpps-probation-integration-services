package uk.gov.justice.digital.hmpps.api.model

import java.time.LocalDate

data class CaseDetail(
    val crn: String,
    val name: Name,
    val dateOfBirth: LocalDate,
    val gender: String?,
    val profile: Profile?,
    val contactDetails: ContactDetails,
)

data class Profile(
    val primaryLanguage: String?,
    val ethnicity: String?,
    val religion: String?,
    val disabilities: List<Disability>,
) {
    companion object {
        fun from(
            primaryLanguage: String?,
            ethnicity: String?,
            religion: String?,
            disabilities: List<Disability>,
        ): Profile? =
            if (primaryLanguage.isNullOrBlank() && ethnicity.isNullOrBlank() && religion.isNullOrBlank() && disabilities.isEmpty()) {
                null
            } else {
                Profile(primaryLanguage, ethnicity, religion, disabilities)
            }
    }
}

data class Disability(
    val type: String,
    val startDate: LocalDate,
    val notes: String?,
)

data class ContactDetails(
    val noFixedAbode: Boolean,
    val mainAddress: Address?,
    val emailAddress: String?,
    val telephoneNumber: String?,
    val mobileNumber: String?,
)
